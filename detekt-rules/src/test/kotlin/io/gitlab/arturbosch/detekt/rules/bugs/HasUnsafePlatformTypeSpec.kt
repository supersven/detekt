package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object HasUnsafePlatformTypeSpec : Spek({
    val subject by memoized { HasUnsafePlatformType(Config.empty) }

    val wrapper by memoized(
        factory = { KtTestCompiler.createEnvironment() },
        destructor = { it.dispose() }
    )

    describe("HasUnsafePlatformType rule") {

        it("reports when a platform type function declaration is unsafe, i.e. can be null but is typed as non-nullable") {
            val code = """
                class Person {
                    fun apiCall():String = System.getProperty("propertyName")
                }
                """
            assertThat(subject.compileAndLintWithContext(wrapper.env, code)).hasSize(1)
        }

        it("reports when a platform type value declaration is unsafe, i.e. can be null but is typed as non-nullable") {
            val code = """
                class Person {
                    val result:String = System.getProperty("propertyName")
                }
                """
            assertThat(subject.compileAndLintWithContext(wrapper.env, code)).hasSize(1)
        }

        it("reports when a platform type variable declaration is unsafe, i.e. can be null but is typed as non-nullable") {
            val code = """
                class Person {
                    var result:String = System.getProperty("propertyName")
                }
                """
            assertThat(subject.compileAndLintWithContext(wrapper.env, code)).hasSize(1)
        }

        it("does not report when a platform type function declaration is safe, i.e. is declared as nullable") {
            val code = """
                class Person {
                    fun apiCall():String? = System.getProperty("propertyName")
                }
                """
            assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
        }

        it("does not report when a platform type value declaration is safe, i.e. is declared as nullable") {
            val code = """
                class Person {
                    val apiCall:String? = System.getProperty("propertyName")
                }
                """
            assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
        }

        it("does not report when a platform type variable declaration is safe, i.e. is declared as nullable") {
            val code = """
                class Person {
                    var apiCall:String? = System.getProperty("propertyName")
                }
                """
            assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
        }

        it("does not report kotlin calls") {
            val code = """
                class Person {
                    fun apiCall():String = true.toString()
                }
                """
            assertThat(subject.compileAndLintWithContext(wrapper.env, code)).isEmpty()
        }
    }
})
