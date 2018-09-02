package io.gitlab.arturbosch.detekt.api.internal

import io.gitlab.arturbosch.detekt.api.Rule
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * LazyRegex class provides a lazy evaluation of a Regex pattern for usages inside Rules.
 * It computes the value once when reaching the point of its usage and returns the same
 * value when requested again.
 *
 * @author Pavlos-Petros Tournaris
 */

class LazyRegex(
		private val key: String,
		private val default: String
) : ReadOnlyProperty<Rule, Regex> {

	private var lazyRegexValue: Regex? = null

	override fun getValue(thisRef: Rule, property: KProperty<*>): Regex {
		return lazyRegexValue ?: createRegex(thisRef).also { lazyRegexValue = it }
	}

	private fun createRegex(rule: Rule): Regex {
		return Regex(rule.config.valueOrDefault(key = key, default = default))
	}
}