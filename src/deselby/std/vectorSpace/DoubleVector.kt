package deselby.std.vectorSpace

import deselby.std.abstractAlgebra.HasTimes

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
                    product.forEach { result.plusAssign(it.key, it.value*thisTerm.value*otherTerm.value) }
                }
            }
            return result
        }
    }
}
