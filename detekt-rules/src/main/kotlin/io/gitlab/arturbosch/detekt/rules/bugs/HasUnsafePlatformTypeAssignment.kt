package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.typeBinding.createTypeBindingForReturnType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isNullable

/**
 * If platform types are declared explicitly, they must be declared as nullable to prevent unexpected errors.
 *
 * Very likely you'll want to use this rule in combination with [HasPlatformType].
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
class HasUnsafePlatformTypeAssignment(config: Config) : Rule(config) {

    override val issue = Issue(
        "HasUnsafePlatformType",
        Severity.Maintainability,
        "If platform types are declared explicitly, they must be declared as nullable.",
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
