package deselby.std.vectorSpace

abstract class AbstractDoubleVector<BASIS> : Vector<BASIS,Double> {

    abstract fun toMutableVector() : AbstractMutableDoubleVector<BASIS>
    abstract fun zero() : AbstractMutableDoubleVector<BASIS>


    override fun unaryMinus(): Vector<BASIS, Double> {
        val result = zero()
        coeffs.mapValuesTo(result.coeffs) { -it.value }
        return result
    }


    override operator fun plus(other : Vector<BASIS, Double>) : Vector<BASIS, Double> {
        val result = toMutableVector()
        other.coeffs.forEach { result += it }
        return result
    }


    override operator fun minus(other: Vector<BASIS, Double>): Vector<BASIS, Double> {
        val result = toMutableVector()
        other.coeffs.forEach { result -= it }
        return result
    }


    override operator fun times(multiplier : Double) : Vector<BASIS, Double> {
        val result = zero()
        if(multiplier != 0.0) {
            coeffs.mapValuesTo(result.coeffs) { it.value * multiplier }
        }
        return result
    }
}

