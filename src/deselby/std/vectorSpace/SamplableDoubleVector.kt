package deselby.std.vectorSpace

import deselby.std.distributions.AbsMutableCategorical
import kotlin.math.sign

class SamplableDoubleVector<BASIS>(val coeffs: AbsMutableCategorical<BASIS> = AbsMutableCategorical()) :
        AbstractMutableMap<BASIS,Double>(), MutableDoubleVector<BASIS> {

    constructor(otherVector : Vector<BASIS,Double>) : this() {
        coeffs.createBinaryTree(
                otherVector.asSequence().map {it.key}.asIterable(),
                otherVector.asSequence().map{it.value}.asIterable())
    }

    override val entries = coeffs.entries

    override fun put(key: BASIS, value: Double) = coeffs.put(key, value)

    override fun get(key: BASIS) = coeffs.get(key)

    override fun remove(key: BASIS) = coeffs.remove(key)

    override fun remove(key: BASIS, value: Double) = coeffs.remove(key, value)

    override fun toMutableVector() = HashDoubleVector(this)

    override fun zero() = HashDoubleVector<BASIS>()

    fun sample() : OneHotDoubleVector<BASIS> {
        val basis = coeffs.sample()
        return OneHotDoubleVector(basis, coeffs[basis].sign)
    }
}