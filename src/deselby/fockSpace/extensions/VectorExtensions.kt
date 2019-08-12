package deselby.fockSpace.extensions

import deselby.fockSpace.*
import deselby.std.vectorSpace.CovariantDoubleVector
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.MutableDoubleVector
import java.util.AbstractMap


//operator fun<AGENT> DoubleVector<CreationBasis<AGENT>>.times(other : DoubleVector<CreationBasis<AGENT>>) =
//        this.vectorMultiplyUsing(other, CreationBasis<AGENT>::times)
//
//operator fun<AGENT> CreationBasis<AGENT>.times(other : DoubleVector<CreationBasis<AGENT>>) =
//        this.timesUsingBasisOperator(other)


typealias CreationVector<AGENT> = DoubleVector<CreationBasis<AGENT>>
typealias AnnihilationVector<AGENT> = DoubleVector<AnnihilationBasis<AGENT>>
typealias OperatorVector<AGENT> = DoubleVector<OperatorBasis<AGENT>>

typealias LazyVector<BASIS> = Sequence<Map.Entry<BASIS,Double>>
typealias LazyBasis<AGENT> = Sequence<Map.Entry<AGENT,Int>>
typealias LazyCreationVector<AGENT> = LazyVector<CreationBasis<AGENT>>
typealias LazyAnnihilationVector<AGENT> = LazyVector<AnnihilationBasis<AGENT>>
//typealias LazyLazyVector<AGENT> = LazyVector<LazyBasis<AGENT>>

interface LazyAnnihilationBasis<AGENT> : Sequence<Map.Entry<AGENT,Int>>
interface LazyCreationBasis<AGENT> : Sequence<Map.Entry<AGENT,Int>>

typealias FockPair<AGENT> = Pair<CreationVector<AGENT>,GroundState<AGENT>>

fun<AGENT> OperatorVector<AGENT>.create(d: AGENT): MutableDoubleVector<OperatorBasis<AGENT>> {
    val result = zero()
    forEach { result[it.key.create(d)] = it.value }
    return result
}


fun<AGENT> OperatorVector<AGENT>.create(d: AGENT, n: Int): MutableDoubleVector<OperatorBasis<AGENT>> {
    val result = zero()
    forEach { result[it.key.create(d,n)] = it.value }
    return result
}


//fun<AGENT> DoubleVector<OperatorBasis<AGENT>>.create(creations: Map<AGENT,Int>): MutableDoubleVector<OperatorBasis<AGENT>> {
//    val result = zero()
//    forEach { result[it.key.create(creations)] = it.value }
//    return result
//}


fun<AGENT> OperatorVector<AGENT>.annihilate(d: AGENT): OperatorVector<AGENT> {
    val result = zero()
    forEach {
        result += it.key.annihilate(d) * it.value
    }
    return result
}


infix fun<AGENT> CreationVector<AGENT>.on(ground : GroundState<AGENT>) = Pair(this,ground)

operator fun<AGENT> LazyCreationBasis<AGENT>.times(basis: CreationVector<AGENT>) = this * basis.asSequence()
operator fun<AGENT> CreationVector<AGENT>.times(basis: LazyCreationBasis<AGENT>) = basis * this.asSequence()

operator fun<AGENT> LazyAnnihilationBasis<AGENT>.times(basis: AnnihilationVector<AGENT>) = this * basis.asSequence()
operator fun<AGENT> AnnihilationVector<AGENT>.times(basis: LazyAnnihilationBasis<AGENT>) = basis * this.asSequence()

operator fun<AGENT> OperatorVector<AGENT>.times(ground : GroundState<AGENT>): CreationVector<AGENT> {
    return timesUsingLazy(this, ground, OperatorBasis<AGENT>::times)
}

operator fun<AGENT> OperatorVector<AGENT>.times(fockState : FockPair<AGENT>): CreationVector<AGENT> {
    return DoubleVector.timesUsing(this, fockState, OperatorBasis<AGENT>::times)
}

///// LazyVector Stuff

operator fun<BASIS> Double.times(v : LazyVector<BASIS>) : LazyVector<BASIS> = v * this
operator fun<BASIS> LazyVector<BASIS>.times(multiplier : Double) : LazyVector<BASIS>
    = this.map { AbstractMap.SimpleEntry(it.key, it.value * multiplier) }

operator fun<BASIS : OperatorSet<*>> DoubleVector<BASIS>.times(multiplier : Double) : LazyVector<BASIS>
        = this.asSequence().map { AbstractMap.SimpleEntry(it.key, it.value * multiplier) }


operator fun<BASIS> MutableDoubleVector<BASIS>.plusAssign(other : LazyVector<BASIS>) {
    other.forEach { this.plusAssign(it) }
}


inline fun<LBASIS, RBASIS, OBASIS> timesUsingLazy(lhs : CovariantDoubleVector<LBASIS>, rhs : RBASIS, operator : (LBASIS, RBASIS) -> LazyVector<OBASIS>) : MutableDoubleVector<OBASIS> {
    val result = HashDoubleVector<OBASIS>()
    lhs.entries.forEach { lhsTerm ->
        result += (operator(lhsTerm.key, rhs) * lhsTerm.value)
    }
    return result
}

// ------------ Act stuff ----------

infix fun<AGENT> ActCreationVector<AGENT>.on(ground: GroundState<AGENT>) = FockState(this, ground)

infix fun<AGENT> MutableActCreationBasis<AGENT>.on(ground: GroundState<AGENT>) = GroundBasis(this, ground)


operator fun<AGENT> CovariantDoubleVector<ActBasis<AGENT>>.times(fockState :FockState<AGENT>) : DoubleVector<ActCreationBasis<AGENT>> {
    val result = HashDoubleVector<ActCreationBasis<AGENT>>()
    this.entries.forEach { thisTerm ->
        fockState.creations.forEach { otherTerm ->
            thisTerm.key.multiplyTo(otherTerm.key, fockState.ground) { basis, weight ->
                result.plusAssign(basis, weight*thisTerm.value * otherTerm.value)
            }
        }
    }
    return result
}

//fun<AGENT> DoubleVector<ActionBasis<AGENT>>.timesActToList(fockState :FockState<AGENT>) : ArrayList<Pair<ActCreationBasis<AGENT>,Double>> {
//    val result = ArrayList<Pair<ActCreationBasis<AGENT>,Double>>(this.size*fockState.creations.size*2)
//    this.forEach { thisTerm ->
//        fockState.creations.forEach { otherTerm ->
//            thisTerm.key.multiplyTo(result, otherTerm.key, fockState.ground, thisTerm.value * otherTerm.value)
//        }
//    }
//    return result
//}

operator fun<AGENT> MutableDoubleVector<ActCreationBasis<AGENT>>.plusAssign(list :ArrayList<Pair<ActCreationBasis<AGENT>,Double>>) {
    list.forEach { this.plusAssign(it.first, it.second) }
}

fun<AGENT> CovariantDoubleVector<ActBasis<AGENT>>.create(d : AGENT) : DoubleVector<ActBasis<AGENT>> {
    val result = HashDoubleVector<ActBasis<AGENT>>()
    this.entries.forEach { result[it.key.create(d)] = it.value }
    return result
}

fun<AGENT> CovariantDoubleVector<ActBasis<AGENT>>.annihilate(d : AGENT) : DoubleVector<ActBasis<AGENT>> {
    val result = HashDoubleVector<ActBasis<AGENT>>()
    this.entries.forEach { result[it.key.annihilate(d)] = it.value }
    return result
}
