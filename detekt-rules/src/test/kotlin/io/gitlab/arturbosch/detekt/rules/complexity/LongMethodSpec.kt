package io.gitlab.arturbosch.detekt.rules.complexity

import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LongMethodSpec : Spek({

    val subject by memoized { LongMethod(threshold = 5) }

    describe("nested functions can be long") {

        it("should find two long methods") {
            val code = """
                fun longMethod() { // 5 lines
                    println()
                    println()
                    println()
            
                    fun nestedLongMethod() { // 5 lines
                        println()
                        println()
                        println()
                    }
                }
            """
            val findings = subject.compileAndLint(code)

            assertThat(findings).hasSize(2)
            assertThat(findings).hasTextLocations(4 to 14, 81 to 97)
        }

        it("should not find too long methods") {
            val code = """
                fun methodOk() { // 3 lines
                    println()
                    fun localMethodOk() { // 4 lines
                        println()
                        println()
                    }
                }
            """

            assertThat(subject.compileAndLint(code)).isEmpty()
        }
    }
})
