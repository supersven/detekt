package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object HasUnsafePlatformTypeAssignmentSpec : Spek({
    val subject by memoized { HasUnsafePlatformTypeAssignment(Config.empty) }

    val wrapper by memoized(
        factory = { KtTestCompiler.createEnvironment() },
        destructor = { it.dispose() }
    )

    describe("HasUnsafePlatformTypeAssignment rule") {

        describe("function declarations") {
            it("reports when a platform type function declaration is unsafe, i.e. can be null but is typed as non-nullable") {
                val code = """
                class SomeClass {
                    fun function():String = System.getProperty("propertyName")
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).hasSize(1)
            }

            it("does not report when a platform type function declaration is safe, i.e. is declared as nullable") {
                val code = """
                class SomeClass {
                    fun function():String? = System.getProperty("propertyName")
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
            }

            it("does not report kotlin calls") {
                val code = """
                class SomeClass {
                    fun function():String = true.toString()
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
            }
        }

        describe("value declaration") {
            it("reports when a platform type value declaration is unsafe, i.e. can be null but is typed as non-nullable") {
                val code = """
                class SomeClass {
                    val value:String = System.getProperty("propertyName")
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).hasSize(1)
            }

            it("does not report when a platform type value declaration is safe, i.e. is declared as nullable") {
                val code = """
                class SomeClass {
                    val value:String? = System.getProperty("propertyName")
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
            }

            it("does not report kotlin calls") {
                val code = """
                class SomeClass {
                    val value:String = true.toString()
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
            }
        }

        describe("variable declaration") {
            it("reports when a platform type variable declaration is unsafe, i.e. can be null but is typed as non-nullable") {
                val code = """
                class SomeClass {
                    var variable:String = System.getProperty("propertyName")
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).hasSize(1)
            }

            it("does not report when a platform type variable declaration is safe, i.e. is declared as nullable") {
                val code = """
                class SomeClass {
                    var variable:String? = System.getProperty("propertyName")
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
            }

            it("does not report kotlin calls") {
                val code = """
                class SomeClass {
                    var variable:String = true.toString()
                }
                """
                assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
            }
        }
    }
})
