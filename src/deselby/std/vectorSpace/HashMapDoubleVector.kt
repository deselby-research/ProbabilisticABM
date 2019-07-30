package deselby.std.vectorSpace

class HashMapDoubleVector<BASIS>(override val coeffs : HashMap<BASIS,Double>) : AbstractMutableDoubleVector<BASIS>() {

    constructor() : this(HashMap())

    constructor(vecToCopy : Vector<BASIS,Double>) : this(HashMap(vecToCopy.coeffs))

    override fun toMutableVector(): AbstractMutableDoubleVector<BASIS> {
        return HashMapDoubleVector(HashMap(coeffs))
    }

    override fun zero(): AbstractMutableDoubleVector<BASIS> {
        return HashMapDoubleVector(HashMap())
    }

}