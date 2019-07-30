package deselby.std.vectorSpace

import deselby.std.distributions.MutableCategorical

class SamplableDoubleVector<BASIS>(override val coeffs: MutableCategorical<BASIS>) : AbstractMutableDoubleVector<BASIS>() {
    override fun toMutableVector(): AbstractMutableDoubleVector<BASIS> {
        return HashMapDoubleVector(this)
    }

    override fun zero(): AbstractMutableDoubleVector<BASIS> {
        return HashMapDoubleVector()
    }
}