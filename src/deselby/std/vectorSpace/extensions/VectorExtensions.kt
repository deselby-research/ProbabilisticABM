package deselby.std.vectorSpace.extensions

import deselby.std.abstractAlgebra.HasPlusMinusAssign
import deselby.std.abstractAlgebra.HasTimes
import deselby.std.vectorSpace.*

//inline fun<BASIS,OTHERBASIS,RESULTBASIS> DoubleVector<BASIS>.vectorMultiply(
//        other : DoubleVector<OTHERBASIS>,
//        multiplyOp : (BASIS,OTHERBASIS)->DoubleVector<RESULTBASIS>) : DoubleVector<RESULTBASIS> {
//    var result : MutableDoubleVector<RESULTBASIS>? = null
//    forEach {thisTerm ->
//        other.forEach { otherTerm ->
//            val basisProduct = multiplyOp(thisTerm.key, otherTerm.key) * (thisTerm.value*otherTerm.value)
//            result?.plusAssign(basisProduct)?:run{ result = basisProduct.toMutableVector() }
//        }
//    }
//    return result?:EmptyDoubleVector()
//}

inline fun<BASIS,OTHERBASIS,RESULT : HasPlusMinusAssign<DoubleVector<OTHERBASIS>>> DoubleVector<BASIS>.vectorMultiplyTo(
        result : RESULT,
        other : DoubleVector<OTHERBASIS>,
        multiplyOp : (BASIS,OTHERBASIS)->DoubleVector<OTHERBASIS>) : RESULT {
    forEach {thisTerm ->
        other.forEach { otherTerm ->
            val basisProduct = multiplyOp(thisTerm.key, otherTerm.key)
            result += basisProduct * (thisTerm.value*otherTerm.value)
        }
    }
    return result
}


operator fun<BASIS : HasTimes<OTHERBASIS, DoubleVector<OTHERBASIS>>, OTHERBASIS> DoubleVector<BASIS>.times(other : DoubleVector<OTHERBASIS>) : DoubleVector<OTHERBASIS> {
    val result = other.zero()
    forEach {thisTerm ->
        other.forEach { otherTerm ->
            result += (thisTerm.key * otherTerm.key) * (thisTerm.value*otherTerm.value)
        }
    }
    return result
}


operator fun<LHS : HasTimes<OTHERBASIS, DoubleVector<OTHERBASIS>>, OTHERBASIS> LHS.times(other : DoubleVector<OTHERBASIS>) : DoubleVector<OTHERBASIS> {
    val result = other.zero()
    other.forEach { otherTerm ->
        result += (this * otherTerm.key) *otherTerm.value
    }
    return result
}
