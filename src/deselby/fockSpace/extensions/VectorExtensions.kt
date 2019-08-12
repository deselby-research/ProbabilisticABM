package deselby.fockSpace.extensions

import deselby.fockSpace.*
import deselby.std.consumerExtensions.asBiconsumer
import deselby.std.vectorSpace.CovariantDoubleVector
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.MutableDoubleVector

fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.create(d: AGENT, n: Int=1) : DoubleVector<Basis<AGENT>> {
    val result = HashDoubleVector<Basis<AGENT>>()
    this.entries.forEach { result[it.key.create(d,n)] = it.value }
    return result
}


fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.annihilate(d : AGENT) : DoubleVector<Basis<AGENT>> {
    val result = HashDoubleVector<Basis<AGENT>>()
    this.entries.forEach { result[it.key.annihilate(d)] = it.value }
    return result
}


infix fun<AGENT> CreationVector<AGENT>.on(ground: GroundState<AGENT>) = FockState(this, ground)

infix fun<AGENT> CreationBasis<AGENT>.on(ground: GroundState<AGENT>) = GroundBasis(this, ground)


operator fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.times(fockState :FockState<AGENT>) : DoubleVector<CreationBasis<AGENT>> {
    val result = HashDoubleVector<CreationBasis<AGENT>>()
    this.entries.forEach { thisTerm ->
        fockState.creationVector.forEach { otherTerm ->
            thisTerm.key.multiplyTo(otherTerm.key, fockState.ground) { basis, weight ->
                result.plusAssign(basis, weight*thisTerm.value * otherTerm.value)
            }
        }
    }
    return result
}


operator fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.times(groundBasis: GroundBasis<AGENT>) : DoubleVector<CreationBasis<AGENT>> {
    val result = HashDoubleVector<CreationBasis<AGENT>>()
    this.entries.forEach { thisTerm ->
        thisTerm.key.multiplyTo(groundBasis) { basis, weight ->
            result.plusAssign(basis, weight*thisTerm.value)
        }
    }
    return result
}

operator fun<AGENT> CreationVector<AGENT>.div(basis: CreationBasis<AGENT>) : CreationVector<AGENT> {
    val result = HashDoubleVector<CreationBasis<AGENT>>()
    this.entries.forEach { result.plusAssign(it.key / basis, it.value) }
    return result
}


operator fun<BASIS> MutableDoubleVector<BASIS>.plusAssign(list: Collection<Pair<BASIS,Double>>) {
    list.forEach { this.plusAssign(it.first, it.second) }
}

// Given a canonical operator, S, will create a mapping from
// agent states, d, to Operators such that d -> a^-_d[a*_d,S]
//
// Uses the identity
// [a*,a^m] = -ma^(m-1)
// so
// a^-_d[a*,a^m] = -m a^-_da^(m-1)
//fun<AGENT> FockVector<AGENT>.toCreationCommutationMap(): CommutationMap<AGENT> {
//    val commutations = HashMap<AGENT, HashDoubleVector<Basis<AGENT>>>()
//    forEach { (basis, basisWeight) ->
//        basis.commutationsTo { d, basis, commutationWeight ->
//            commutations.getOrPut(d, { HashDoubleVector() }).
//                    plusAssign(basis.create(d,-1), basisWeight*commutationWeight)
//        }
//    }
//    return commutations
//}


fun<AGENT> FockVector<AGENT>.toAnnihilationIndex(): AnnihilationIndex<AGENT> {
    val index = HashMap<AGENT, ArrayList<Map.Entry<Basis<AGENT>,Double>>>()
    forEach { entry ->
        entry.key.forEachAnnihilationKey { d ->
            index.getOrPut(d, { ArrayList() }).add(entry)
        }
    }
    return index
}


fun<AGENT> AnnihilationIndex<AGENT>.commute(otherBasis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
    val activeTerms = HashSet<Map.Entry<Basis<AGENT>,Double>>()
    otherBasis.creations.keys.forEach { d ->
        this[d]?.forEach { term ->
            activeTerms.add(term)
        }
    }
    activeTerms.forEach { (indexedBasis, indexedWeight) ->
        indexedBasis.commute(otherBasis) { commutedBasis, commutedWeight ->
            termConsumer(commutedBasis, commutedWeight * indexedWeight)
        }
    }
}


fun<AGENT> AnnihilationIndex<AGENT>.commute(otherBasis: CreationBasis<AGENT>) : FockVector<AGENT> {
    val commutation = HashFockVector<AGENT>()
    this.commute(otherBasis, commutation.asBiconsumer())
    return commutation
}

fun<AGENT> FockState<AGENT>.integrate(hamiltonian: FockVector<AGENT>, T: Double, dt: Double) : CreationVector<AGENT> {
    val Hdt  = hamiltonian*dt
    val state = HashDoubleVector(this.creationVector)
    var time = 0.0
    while(time < T) {
        state += Hdt * (state on ground)
        time += dt
    }
    return state
}
//fun<AGENT> CommutationMap<AGENT>.commute(basis: CreationBasis<AGENT>): FockVector<AGENT> {
//    basis.creationVector.forEach {
//        val commutation = this[it.key] ?: Basis.identityVector()
//        if (it.value > 0) {
//            for (i in 1..it.value) {
//                val Q = commutation * sample
//                possibleTransitionStates -= Q
//                sampleBasis.createAssign(it.key, 1)
//            }
//        } else if (it.value < 0) {
//            for (i in 1..-it.value) {
//                sampleBasis.createAssign(it.key, -1)
//                val Q = commutation * sample
//                possibleTransitionStates += Q
//            }
//        }
//    }
//
//}