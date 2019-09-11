package deselby.std.vectorSpace

import deselby.std.collections.DoubleArrayMap

class ArrayDoubleVector<BASIS>(val coeffs : DoubleArrayMap<BASIS>) :
        AbstractMutableMap<BASIS,Double>(),
        MutableDoubleVector<BASIS>,
        DenseVector {

    override val entries
            get() = coeffs.entries

    override fun put(key: BASIS, value: Double) = coeffs.put(key,value)

    override fun get(key: BASIS) = coeffs.get(key)

    override fun toMutableVector()= ArrayDoubleVector(DoubleArrayMap(coeffs))

    override fun zero()= ArrayDoubleVector(DoubleArrayMap(DoubleArray(coeffs.size) { 0.0 }, coeffs.keyToIndex, coeffs.keysInOrder))
}