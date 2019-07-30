package deselby.std.vectorSpace

import deselby.std.collections.DoubleArrayMap

class ArrayDoubleVector<BASIS>(override val coeffs : DoubleArrayMap<BASIS>) :
        AbstractMutableDoubleVector<BASIS>() {

    override fun toMutableVector(): AbstractMutableDoubleVector<BASIS> {
        return ArrayDoubleVector(DoubleArrayMap(coeffs))
    }

    override fun zero(): AbstractMutableDoubleVector<BASIS> {
        return ArrayDoubleVector(DoubleArrayMap(DoubleArray(coeffs.size) { 0.0 }, coeffs.keyToIndex, coeffs.keysInOrder))
    }
}