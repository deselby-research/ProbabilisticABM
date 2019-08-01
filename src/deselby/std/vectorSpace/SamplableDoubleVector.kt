package deselby.std.vectorSpace

import deselby.std.distributions.AbsMutableCategorical
import kotlin.math.sign

class SamplableDoubleVector<BASIS>(val coeffs: AbsMutableCategorical<BASIS> = AbsMutableCategorical()) :
        AbstractMutableMap<BASIS,Double>(), MutableDoubleVector<BASIS> {

    constructor(otherVector : Vector<BASIS,Double>) : this() {
//        coeffs.putAll(otherVector)
        coeffs.createBinaryTree(otherVector.keys, otherVector.values)
    }

    override val entries = coeffs.entries

    override fun put(key: BASIS, value: Double) = coeffs.put(key, value)

    override fun get(key: BASIS) = coeffs.get(key)

    override fun remove(key: BASIS) = coeffs.remove(key)

    override fun remove(key: BASIS, value: Double) = coeffs.remove(key, value)

    override fun toMutableVector() = HashMapDoubleVector(this)

    override fun zero() = HashMapDoubleVector<BASIS>()

    fun sample() : OneHotDoubleVector<BASIS> {
        val basis = coeffs.sample()
        return OneHotDoubleVector(basis, coeffs[basis].sign)
    }
}