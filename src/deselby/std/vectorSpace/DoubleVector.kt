package deselby.std.vectorSpace

import deselby.std.abstractAlgebra.HasTimes
import kotlin.math.abs
import kotlin.math.absoluteValue

interface DoubleVector<BASIS> : CovariantDoubleVector<BASIS>, Vector<BASIS,Double> {

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

    operator fun div(divisor : Double) : DoubleVector<BASIS> {
        val result = zero()
        mapValuesTo(result) { it.value / divisor }
        return result
    }

    // L1-norm is defined as the sum of absolute values of the coefficients
    fun normL1() = values.sumByDouble(::abs)

    fun normLinfty() = values.asSequence().map { abs(it) }.max()?:0.0

    fun filterBelow(upperLimit: Double): DoubleVector<BASIS> {
        val filtered = zero()
        entries.forEach {
            if(it.value.absoluteValue > upperLimit) filtered[it.key] = it.value
        }
        return filtered
    }

    companion object {

        inline fun<LBASIS, RBASIS, OBASIS> times(lhs : LBASIS, rhs : CovariantDoubleVector<RBASIS>, operator : (LBASIS, RBASIS) -> OBASIS) : MutableDoubleVector<OBASIS> {
            val result = HashDoubleVector<OBASIS>()
            rhs.entries.forEach { rhsTerm -> result.plusAssign(operator(lhs, rhsTerm.key), rhsTerm.value) }
            return result
        }


        inline fun<LBASIS, RBASIS, OBASIS> times(lhs : CovariantDoubleVector<LBASIS>, rhs : RBASIS, operator : (LBASIS, RBASIS) -> OBASIS) : MutableDoubleVector<OBASIS> {
            val result = HashDoubleVector<OBASIS>()
            lhs.entries.forEach { lhsTerm -> result.plusAssign(operator(lhsTerm.key, rhs), lhsTerm.value) }
            return result
        }


        inline fun<LBASIS, RBASIS, OBASIS> times(lhs : CovariantDoubleVector<LBASIS>, rhs : CovariantDoubleVector<RBASIS>, operator : (LBASIS, RBASIS) -> OBASIS) : MutableDoubleVector<OBASIS> {
            val result = HashDoubleVector<OBASIS>()
            lhs.entries.forEach {thisTerm ->
                rhs.entries.forEach { otherTerm ->
                    result.plusAssign(operator(thisTerm.key, otherTerm.key), (thisTerm.value * otherTerm.value))
                }
            }
            return result
        }


        inline fun<LBASIS, RBASIS, OBASIS> timesUsing(lhs : LBASIS, rhs : CovariantDoubleVector<RBASIS>, operator : (LBASIS, RBASIS) -> DoubleVector<OBASIS>) : MutableDoubleVector<OBASIS> {
            val result = HashDoubleVector<OBASIS>()
            rhs.entries.forEach { rhsTerm ->
                val product = operator(lhs, rhsTerm.key)
                product.forEach { result.plusAssign(it.key, it.value*rhsTerm.value) }
            }
            return result
        }


        inline fun<LBASIS, RBASIS, OBASIS> timesUsing(lhs : CovariantDoubleVector<LBASIS>, rhs : RBASIS, operator : (LBASIS, RBASIS) -> DoubleVector<OBASIS>) : MutableDoubleVector<OBASIS> {
            val result = HashDoubleVector<OBASIS>()
            lhs.entries.forEach { lhsTerm ->
                val product = operator(lhsTerm.key, rhs)
                product.forEach { result.plusAssign(it.key, it.value*lhsTerm.value) }
            }
            return result
        }


        inline fun<LBASIS, RBASIS, OBASIS> timesUsing(lhs : CovariantDoubleVector<LBASIS>, rhs : CovariantDoubleVector<RBASIS>, operator : (LBASIS, RBASIS) -> DoubleVector<OBASIS>) : MutableDoubleVector<OBASIS> {
            val result = HashDoubleVector<OBASIS>()
            lhs.entries.forEach {thisTerm ->
                rhs.entries.forEach { otherTerm ->
                    val product = operator(thisTerm.key, otherTerm.key)
                    val coeffProduct = thisTerm.value*otherTerm.value
                    product.forEach { result.plusAssign(it.key, it.value*coeffProduct) }
                }
            }
            return result
        }


        inline fun<LBASIS, RBASIS, OBASIS> timesApproximate(lhs : CovariantDoubleVector<LBASIS>, rhs : CovariantDoubleVector<RBASIS>, coeffLowerBound: Double, operator : (LBASIS, RBASIS) -> OBASIS) : MutableDoubleVector<OBASIS> {
            val result = HashDoubleVector<OBASIS>()
            lhs.entries.forEach {thisTerm ->
                rhs.entries.forEach { otherTerm ->
                    val coeffProduct = thisTerm.value * otherTerm.value
                    if(abs(coeffProduct) > coeffLowerBound) result.plusAssign(operator(thisTerm.key, otherTerm.key), coeffProduct)
                }
            }
            return result
        }


        inline fun<LBASIS, RBASIS, OBASIS> timesApproximateUsing(lhs : CovariantDoubleVector<LBASIS>, rhs : CovariantDoubleVector<RBASIS>, coeffLowerBound: Double, operator : (LBASIS, RBASIS) -> DoubleVector<OBASIS>) : MutableDoubleVector<OBASIS> {
            val result = HashDoubleVector<OBASIS>()
            lhs.entries.forEach {thisTerm ->
                rhs.entries.forEach { otherTerm ->
                    val product = operator(thisTerm.key, otherTerm.key)
                    product.forEach {
                        val coeffProduct = it.value*thisTerm.value*otherTerm.value
                        if(abs(coeffProduct) > coeffLowerBound) result.plusAssign(it.key, coeffProduct)
                    }
                }
            }
            return result
        }

    }
}
