package io.gitlab.arturbosch.detekt.rules.naming

import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileContentForTest
import io.gitlab.arturbosch.detekt.test.lint
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class MatchingDeclarationNameSpec : Spek({

    describe("MatchingDeclarationName rule") {

        context("compliant test cases") {

            it("should pass for object declaration") {
                val ktFile = compileContentForTest("object O", filename = "O.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }

            it("should pass for suppress") {
                val ktFile = compileContentForTest(
                    """@file:Suppress("MatchingDeclarationName") object O""",
                    filename = "Objects.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }

            it("should pass for class declaration") {
                val ktFile = compileContentForTest("class C", filename = "C.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }

            it("should pass for interface declaration") {
                val ktFile = compileContentForTest("interface I", filename = "I.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }

            it("should pass for enum declaration") {
                val ktFile = compileContentForTest("""
                enum class E {
                    ONE, TWO, THREE
                }
            """, filename = "E.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }

            it("should pass for multiple declaration") {
                val ktFile = compileContentForTest("""
                class C
                object O
                fun a() = 5
            """, filename = "MultiDeclarations.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }

            it("should pass for class declaration with utility functions") {
                val ktFile = compileContentForTest("""
                class C
                fun a() = 5
                fun C.b() = 5
            """, filename = "C.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }

            it("should pass for private class declaration") {
                val ktFile = compileContentForTest("""
                private class C
                fun a() = 5
            """, filename = "b.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }

            it("should pass for a class with a typealias") {
                val code = """
                typealias Foo = FooImpl

                class FooImpl {}"""
                val ktFile = compileContentForTest(code, filename = "Foo.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).isEmpty()
            }
        }

        context("non-compliant test cases") {

            it("should not pass for object declaration") {
                val ktFile = compileContentForTest("object O", filename = "Objects.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).hasLocationStrings("'object O' at (1,1) in /Objects.kt")
            }

            it("should not pass for object declaration even with suppress on the object") {
                val ktFile = compileContentForTest(
                    """@Suppress("MatchingDeclarationName") object O""",
                    filename = "Objects.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).hasLocationStrings(
                    """'@Suppress("MatchingDeclarationName") object O' at (1,1) in /Objects.kt""")
            }

            it("should not pass for class declaration") {
                val ktFile = compileContentForTest("class C", filename = "Classes.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).hasLocationStrings("'class C' at (1,1) in /Classes.kt")
            }

            it("should not pass for class declaration with utility functions") {
                val ktFile = compileContentForTest("""
                fun a() = 5
                class C
                fun C.b() = 5

            """.trimIndent(), filename = "ClassUtils.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).hasLocationStrings("'class C' at (2,1) in /ClassUtils.kt")
            }

            it("should not pass for interface declaration") {
                val ktFile = compileContentForTest("interface I", filename = "Not_I.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).hasLocationStrings("'interface I' at (1,1) in /Not_I.kt")
            }

            it("should not pass for enum declaration") {
                val ktFile = compileContentForTest("""
                enum class NOT_E {
                    ONE, TWO, THREE
                }

            """.trimIndent(), filename = "E.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).hasLocationStrings("""
                'enum class NOT_E {
                    ONE, TWO, THREE
                }' at (1,1) in /E.kt""", trimIndent = true)
            }

            it("should not pass for a typealias with a different name") {
                val code = """
                typealias Bar = FooImpl

                class FooImpl {}"""
                val ktFile = compileContentForTest(code, filename = "Foo.kt")
                val findings = MatchingDeclarationName().lint(ktFile)
                assertThat(findings).hasSize(1)
            }
        }
    }
})
