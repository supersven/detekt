package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class UnusedPrivateClassSpec : Spek({

    val subject by memoized { UnusedPrivateClass() }

    describe("top level interfaces") {
        it("should report them if not used") {
            val code = """
                private interface Foo
                class Bar
                """

            val lint = subject.lint(code)

            assertThat(lint).hasSize(1)
            with(lint[0].entity) {
                assertThat(ktElement?.text).isEqualTo("private interface Foo")
            }
        }

        describe("top level private classes") {

            it("should report them if not used") {
                val code = """
                private class Foo
                class Bar
                """

                val lint = subject.lint(code)

                assertThat(lint).hasSize(1)
                with(lint[0].entity) {
                    assertThat(ktElement?.text).isEqualTo("private class Foo")
                }
            }

            it("should not report them if used as parent") {
                val code = """
                private class Foo
                private class Bar : Foo
                """

                val lint = subject.lint(code)

                assertThat(lint).hasSize(1)
                with(lint[0].entity) {
                    assertThat(ktElement?.text).isEqualTo("private class Bar : Foo")
                }
            }

            it("should not report them used as generic parent type") {
                val code = """
                private class Foo<in Bar> {
                    operator fun invoke(b: Bar): Unit
                }

                data class FooOne(val b: Bar2) : Foo<Bar2> {
                    override fun invoke(b: Bar2): Unit = Unit
                }
                """

                val lint = subject.lint(code)

                assertThat(lint).isEmpty()
            }
        }

        it("should not report them if used inside a function") {
            val code = """
                private class Foo
                fun something() {
                    val foo: Foo = Foo()
                }
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as function parameter") {
            val code = """
                private class Foo
                fun bar(foo: Foo) = Unit
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as nullable variable type") {
            val code = """
                private class Foo
                val a: Foo? = null
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as variable type") {
            val code = """
                private class Foo
                lateinit var a: Foo
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as generic type") {
            val code = """
                private class Foo
                lateinit var foos: List<Foo>
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as nested generic type") {
            val code = """
                private class Foo
                lateinit var foos: List<List<Foo>>
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as type with generics") {
            val code = """
                private class Foo<T>
                lateinit var foos: Foo<String>
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as nullable type with generics") {
            val code = """
                private class Foo<T>
                lateinit var foos: Foo<String>?
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as non-argument constructor") {
            val code = """
                private class Foo
                val a = Foo()
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as constructor with arguments") {
            val code = """
                private class Foo(val a: String)
                val a = Foo("test")
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as function return type") {
            val code = """
                private class Foo(val a: String)
                fun foo(): Foo? = null
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as lambda declaration parameter") {
            val code = """
                private class Foo
                val lambda: ((Foo) -> Unit)? = null
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as lambda declaration return type") {
            val code = """
                private class Foo
                val lambda: (() -> Foo)? = null
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as lambda declaration generic type") {
            val code = """
                private class Foo
                val lambda: (() -> List<Foo>)? = null
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }

        it("should not report them if used as inline object type") {
            val code = """
                private abstract class Foo {
                    abstract fun bar()
                }

                private fun foo() = object : Foo() {
                    override fun bar() = Unit
                }
                """

            val lint = subject.lint(code)

            assertThat(lint).isEmpty()
        }
    }

    describe("testcase for reported false positives") {

        it("does not crash when using wildcards in generics - #1345") {
            val code = """
                private class Foo
                fun bar(clazz: KClass<*>) = Unit
            """

            val findings = UnusedPrivateClass().lint(code)

            assertThat(findings).hasSize(1)
        }

        it("does not report (companion-)object/named-dot references - #1347") {
            val code = """
                    package com.example

                    class Test {
                        val items = Item.values().map { it.text }.toList()
                    }

                    private enum class Item(val text: String) {
                        A("A"),
                        B("B"),
                        C("C")
                    }
                """

            val findings = UnusedPrivateClass().lint(code)

            assertThat(findings).isEmpty()
        }

        it("does not report classes that are used with ::class - #1390") {
            val code = """
                    class UnusedPrivateClassTest {

                        private data class SomeClass(val name: String)

                        private data class AnotherClass(val id: Long)

                        @Test
                        fun `verify class is used`() {
                            val instance = SomeClass(name = "test")
                            assertNotEquals(AnotherClass::class.java.simpleName, instance::class.java.simpleName)
                        }

                        fun getSomeObject(): ((String) -> Any) = ::InternalClass
                        private class InternalClass(val param: String)
                    }
                """

            val findings = UnusedPrivateClass().lint(code)

            assertThat(findings).isEmpty()
        }
    }
})
