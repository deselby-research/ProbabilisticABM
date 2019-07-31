package deselby.std.vectorSpace

interface DoubleVector<BASIS> : Vector<BASIS,Double> {

    override fun zero(): MutableDoubleVector<BASIS>
    override fun toMutableVector(): MutableDoubleVector<BASIS>

    override fun unaryMinus(): DoubleVector<BASIS> {
        val result = zero()
        mapValuesTo(result) { -it.value }
        return result
    }


    override operator fun plus(other : Vector<BASIS, Double>) : DoubleVector<BASIS> {
        val result = toMutableVector()
        other.forEach { result += it }
        return result
    }


    override operator fun minus(other: Vector<BASIS, Double>): DoubleVector<BASIS> {
        val result = toMutableVector()
        other.forEach { result -= it }
        return result
    }


    override operator fun times(multiplier : Double) : DoubleVector<BASIS> {
        val result = zero()
        if(multiplier != 0.0) {
            mapValuesTo(result) { it.value * multiplier }
        }
        return result
    }
}