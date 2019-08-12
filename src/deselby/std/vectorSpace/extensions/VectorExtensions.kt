package deselby.std.vectorSpace.extensions

import deselby.std.abstractAlgebra.HasPlusMinusAssign
import deselby.std.abstractAlgebra.HasTimes
import deselby.std.abstractAlgebra.HasTimesBySelf
import deselby.std.vectorSpace.*
import java.util.AbstractMap

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


inline fun<LBASIS,RBASIS,OBASIS> DoubleVector<LBASIS>.times(other : DoubleVector<RBASIS>, op : (LBASIS,RBASIS)->OBASIS) : MutableDoubleVector<OBASIS>
    = DoubleVector.times(this, other, op)

inline fun<LBASIS,RBASIS,OBASIS> DoubleVector<LBASIS>.times(other : RBASIS, op : (LBASIS,RBASIS)->OBASIS) : MutableDoubleVector<OBASIS>
    = DoubleVector.times(this, other, op)

operator fun<LBASIS : HasTimes<RBASIS,OUTBASIS>,RBASIS,OUTBASIS> DoubleVector<LBASIS>.times(other : DoubleVector<RBASIS>) : DoubleVector<OUTBASIS>
     = DoubleVector.times<LBASIS,RBASIS,OUTBASIS>(this, other, HasTimes<RBASIS,OUTBASIS>::times)

operator fun<RBASIS,OBASIS> CovariantDoubleVector<HasDoubleVectorTimes<RBASIS,OBASIS>>.times(other : DoubleVector<RBASIS>) : DoubleVector<OBASIS>
    = DoubleVector.timesUsing<HasDoubleVectorTimes<RBASIS,OBASIS>,RBASIS,OBASIS>(this, other, HasDoubleVectorTimes<RBASIS,OBASIS>::times)

operator fun<LBASIS : HasTimes<RBASIS,OBASIS>,RBASIS,OBASIS> DoubleVector<LBASIS>.times(other : RBASIS) : MutableDoubleVector<OBASIS>
    = DoubleVector.times(this, other, HasTimes<RBASIS,OBASIS>::times)

operator fun<RBASIS,OBASIS> CovariantDoubleVector<HasDoubleVectorTimes<RBASIS,OBASIS>>.times(other : RBASIS) : MutableDoubleVector<OBASIS>
    = DoubleVector.timesUsing(this, other, HasDoubleVectorTimes<RBASIS,OBASIS>::times)

operator fun<RBASIS,OBASIS> HasDoubleVectorTimes<RBASIS, OBASIS>.times(other : DoubleVector<RBASIS>) : MutableDoubleVector<OBASIS>
    = DoubleVector.timesUsing(this, other, HasDoubleVectorTimes<RBASIS,OBASIS>::times)

operator fun<RBASIS,OBASIS> HasTimes<RBASIS, OBASIS>.times(other : DoubleVector<RBASIS>) : MutableDoubleVector<OBASIS>
    = DoubleVector.times(this, other, HasTimes<RBASIS,OBASIS>::times)

fun<BASIS> MutableDoubleVector<BASIS>.integrate(hamiltonian : (DoubleVector<BASIS>)-> DoubleVector<BASIS>, T : Double, dt : Double) {
    var time = 0.0
    while(time < T) {
        this += hamiltonian(this)*dt
        time += dt
    }
}

//operator fun<BASIS : HasTimesBySelf<BASIS>> HasTimesBySelf<BASIS>.times(other : DoubleVector<BASIS>) : MutableDoubleVector<BASIS> {


//fun<BASIS : HasTimesBySelf<BASIS>> DoubleVector<BASIS>.timesUsingBasisTimesBySelf(other : DoubleVector<BASIS>) =
//    this.vectorMultiplyUsing(other, HasTimesBySelf<BASIS>::times)
//


//fun<RBASIS,OBASIS> HasTimes<RBASIS, OBASIS>.timesUsingBasisOperator(other : DoubleVector<RBASIS>) : MutableDoubleVector<OBASIS> {
//    val result = HashDoubleVector<OBASIS>()
//    other.mapKeysTo(result) { otherTerm -> this * otherTerm.key }
//    return result
//}



//operator fun<BASIS> DoubleVector<BASIS>.times(other : HasTimes<BASIS, DoubleVector<BASIS>>) : DoubleVector<BASIS> {
//    val result = zero()
//    forEach { thisTerm ->
//        result += (other * thisTerm.key) * thisTerm.value
//    }
//    return result
//}
