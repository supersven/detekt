package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.typeBinding.createTypeBindingForReturnType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isNullable

/**
 * Platform types must be declared explicitly as nullable to prevent unexpected errors.
 *
 * <noncompliant>
 * class Person {
 *   fun apiCall():String = System.getProperty("propertyName")
 * }
 * </noncompliant>
 *
 * <compliant>
 * class Person {
 *   fun apiCall(): String? = System.getProperty("propertyName")
 * }
 * </compliant>
 */
// Todo: Detect type coercions in argument position:
//  fun foo(a : String)
//  foo (badJavaCall()) -- badJavaCall() : String?
class HasUnsafePlatformType(config: Config) : Rule(config) {

    override val issue = Issue(
        "HasUnsafePlatformType",
        Severity.Maintainability,
        "Platform types must be declared explicitly as nullable.",
        Debt.FIVE_MINS
    )

    override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
        super.visitNamedDeclaration(declaration)

        if (bindingContext != BindingContext.EMPTY && declaration is KtCallableDeclaration &&
            declaration.hidesTypeNullability()
        ) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(declaration),
                    "$declaration has a platform type which is nullable. Please adjust your type accordingly."
                )
            )
        }
    }

    @Suppress("ReturnCount", "ComplexMethod")
    private fun KtCallableDeclaration.hidesTypeNullability(): Boolean {
        return when (this) {
            is KtFunction -> {
                val boundType = this.createTypeBindingForReturnType(bindingContext)?.type
                val expressionType = this.bodyExpression?.getType(bindingContext)

                typeHidesNullabilityOfOther(boundType, expressionType)
            }
            is KtProperty -> {
                val boundType = this.createTypeBindingForReturnType(bindingContext)?.type
                val expressionType = this.initializer?.getType(bindingContext)

                typeHidesNullabilityOfOther(boundType, expressionType)
            }
            else -> false
        }
    }

    private fun typeHidesNullabilityOfOther(
        type: KotlinType?,
        otherType: KotlinType?
    ): Boolean {
        return if (otherType?.isNullable() == true) {
            type?.isNullable() == false
        } else {
            false
        }
    }
}
