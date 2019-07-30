package deselby.std.vectorSpace

class OneHotDoubleVector<BASIS>(val basis : BASIS, val coeff : Double) : Vector<BASIS,Double> {

    override val coeffs = mapOf(basis to coeff)


    override operator fun plus(other: Vector<BASIS,Double>): HashMapDoubleVector<BASIS> {
        val result = HashMapDoubleVector(other)
        result.coeffs.merge(basis , coeff) { a, b ->
            val newVal = a + b
            if(newVal == 0.0) null else newVal
        }
        return result
    }


    override operator fun minus(other: Vector<BASIS,Double>): HashMapDoubleVector<BASIS> {
        val result = HashMapDoubleVector(other)
        result.coeffs.merge(basis , coeff) { a, b ->
            val newVal = b - a
            if(newVal == 0.0) null else newVal
        }
        return result
    }


    override operator fun unaryMinus(): Vector<BASIS,Double> =
            OneHotDoubleVector(basis, -coeff)


    override operator fun times(multiplier: Double): OneHotDoubleVector<BASIS> =
            OneHotDoubleVector(basis, coeff*multiplier)


    override fun toString(): String {
        return "%+fP[%s]".format(coeff, basis)
    }
}
