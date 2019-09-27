package deselby.fockSpace.extensions

import deselby.fockSpace.*
import deselby.std.vectorSpace.CovariantDoubleVector
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.MutableDoubleVector
import org.apache.commons.math3.special.Gamma
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.min



fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.create(d: AGENT, n: Int=1) : DoubleVector<Basis<AGENT>> {
    val result = HashDoubleVector<Basis<AGENT>>()
    this.entries.forEach { result[it.key.create(d,n)] = it.value }
    return result
}

fun<AGENT> DoubleVector<CreationBasis<AGENT>>.create(d: AGENT, n: Int=1) : DoubleVector<CreationBasis<AGENT>> {
    val result = HashDoubleVector<CreationBasis<AGENT>>()
    this.entries.forEach { result[it.key.create(d,n)] = it.value }
    return result
}


fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.annihilate(d : AGENT) : DoubleVector<Basis<AGENT>> {
    val result = HashDoubleVector<Basis<AGENT>>()
    this.entries.forEach { thisTerm ->
        thisTerm.key.annihilate(d).forEach { annihilationTerm ->
            result.plusAssign(annihilationTerm.key, annihilationTerm.value*thisTerm.value)
        }
    }
    return result
}


fun<AGENT> CovariantFockVector<AGENT>.toAnnihilationIndex(): AnnihilationIndex<AGENT> {
    val index = HashMap<AGENT, ArrayList<Map.Entry<Basis<AGENT>,Double>>>()
    entries.forEach { entry ->
        entry.key.annihilations.keys.forEach { d ->
            index.getOrPut(d, { ArrayList() }).add(entry)
        }
    }
    return index
}


fun<AGENT> CovariantFockVector<AGENT>.toCreationIndex(): CreationIndex<AGENT> {
    val index = HashMap<AGENT, ArrayList<Map.Entry<Basis<AGENT>,Double>>>()
    entries.forEach { entry ->
        entry.key.creations.keys.forEach { d ->
            index.getOrPut(d, { ArrayList() }).add(entry)
        }
    }
    return index
}

fun<AGENT> CovariantFockVector<AGENT>.stripCreations(): FockVector<AGENT> {
    val stripped = HashFockVector<AGENT>()
    entries.forEach { entry ->
        stripped.plusAssign(Basis.newBasis(emptyMap(), entry.key.annihilations), entry.value)
    }
    return stripped
}



fun<AGENT> FockVector<AGENT>.semicommute(creationIndex: CreationIndex<AGENT>) : FockVector<AGENT> {
    val commutation = HashFockVector<AGENT>()
    forEach { termBasis, termWeight ->
        termBasis.semicommute(creationIndex) { commutedBasis, commutedWeight ->
            commutation.plusAssign(commutedBasis, commutedWeight * termWeight)
        }
    }
    return commutation
}


inline fun<AGENT> FockVector<AGENT>.semicommute(creationIndex: CreationIndex<AGENT>, crossinline termConsumer: (Basis<AGENT>, Double) -> Unit) {
    forEach { termBasis, termWeight ->
        termBasis.semicommute(creationIndex) { commutedBasis, commutedWeight ->
            termConsumer(commutedBasis, commutedWeight * termWeight)
        }
    }
}


fun<AGENT> FockVector<AGENT>.semiCommuteAndStrip(creationIndex: CreationIndex<AGENT>) : FockVector<AGENT> {
    val commutation = HashFockVector<AGENT>()
    forEach { termBasis, termWeight ->
        termBasis.semiCommuteAndStrip(creationIndex) { commutedBasis, commutedWeight ->
            commutation.plusAssign(commutedBasis, commutedWeight * termWeight)
        }
    }
    return commutation
}



operator fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.times(groundBasis: Ground<AGENT>) : DoubleVector<CreationBasis<AGENT>> {
    val result = HashDoubleVector<CreationBasis<AGENT>>()
    this.entries.forEach { thisTerm ->
        thisTerm.key.multiply(groundBasis) { basis, weight ->
            result.plusAssign(basis, weight*thisTerm.value)
        }
    }
    return result
}


operator fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.times(creationVector: CreationVector<AGENT>) : FockVector<AGENT> {
    val result = HashDoubleVector<Basis<AGENT>>()
    this.entries.forEach { thisTerm ->
        creationVector.entries.forEach {otherTerm ->
            val vectorWeight = thisTerm.value * otherTerm.value
            thisTerm.key.multiply(otherTerm.key) { basis, weight ->
                result.plusAssign(basis, weight * vectorWeight)
            }
        }
    }
    return result
}

operator fun<AGENT> CreationVector<AGENT>.times(creationVector: CreationVector<AGENT>) : CreationVector<AGENT> {
    val result = HashCreationVector<AGENT>()
    this.entries.forEach { thisTerm ->
        creationVector.entries.forEach {otherTerm ->
            val vectorWeight = thisTerm.value * otherTerm.value
            thisTerm.key.multiply(otherTerm.key) { basis, weight ->
                result.plusAssign(basis as CreationBasis<AGENT>, weight * vectorWeight)
            }
        }
    }
    return result
}


operator fun<AGENT, BASIS: Ground<AGENT>> FockVector<AGENT>.times(groundedVector: GroundedVector<AGENT, BASIS>): GroundedVector<AGENT,BASIS> {
    return GroundedVector(this.vectorMultiply<AGENT,Basis<AGENT>, CreationBasis<AGENT>, CreationBasis<AGENT>>(groundedVector.creationVector) { lhs, rhs, termConsumer ->
        lhs.multiplyAndGround(rhs, groundedVector.ground, termConsumer)
    }, groundedVector.ground)
}


fun<AGENT, BASIS: Ground<AGENT>> FockVector<AGENT>.timesAndMarginalise(groundedVector: GroundedVector<AGENT, BASIS>, activeAgents: Set<AGENT>): GroundedVector<AGENT,BASIS> {
    return GroundedVector(this.vectorMultiply<AGENT,Basis<AGENT>, CreationBasis<AGENT>, CreationBasis<AGENT>>(groundedVector.creationVector) { lhs, rhs, termConsumer ->
        lhs.multiplyGroundAndMarginalise(rhs, groundedVector.ground, activeAgents, termConsumer)
    }, groundedVector.ground)
}


fun<AGENT> FockVector<AGENT>.timesAndMarginalise(groundedBasis: Ground<AGENT>, activeAgents: Set<AGENT>): CreationVector<AGENT> {
    val marginalisation = HashCreationVector<AGENT>()
    this.forEach {(basis, weight) ->
        groundedBasis.preMultiply(basis) { groundedBasis, groundedWeight ->
            marginalisation.plusAssign(groundedBasis.marginalise(activeAgents), groundedWeight * weight)
        }
    }
    return marginalisation
}


operator fun<AGENT> FockVector<AGENT>.times(rhs: CreationBasis<AGENT>) : FockVector<AGENT> {
    val result = HashDoubleVector<Basis<AGENT>>()
    this.entries.forEach { lhsTerm ->
        lhsTerm.key.multiply(rhs) { basis, weight ->
            result.plusAssign(basis, weight*lhsTerm.value)
        }
    }
    return result
}


operator fun<AGENT> CovariantFockVector<AGENT>.times(rhs: CovariantFockVector<AGENT>) : FockVector<AGENT> {
    val result = HashDoubleVector<Basis<AGENT>>()
    this.entries.forEach { lhsTerm ->
        rhs.entries.forEach { rhsTerm ->
            val termWeight = lhsTerm.value * rhsTerm.value
            lhsTerm.key.multiply(rhsTerm.key) { basis, weight ->
                result.plusAssign(basis, weight * termWeight)
            }
        }
    }
    return result
}


operator fun<AGENT> FockVector<AGENT>.times(rhs: Basis<AGENT>) : FockVector<AGENT> {
    val multiplied = HashDoubleVector<Basis<AGENT>>()
    this.forEach { (thisBasis, thisWeight) ->
        multiplied.plusAssign(thisBasis.union(rhs), thisWeight)
        thisBasis.semicommute(rhs) { commutedBasis, cWeight ->
            multiplied.plusAssign(commutedBasis, cWeight * thisWeight)
        }
    }
    return multiplied
}


fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.timesApproximate(creationVector: CreationVector<AGENT>, coeffLowerBound: Double) : FockVector<AGENT> {
    val result = HashDoubleVector<Basis<AGENT>>()
    this.entries.forEach { thisTerm ->
        creationVector.entries.forEach {otherTerm ->
            val vectorWeight = thisTerm.value * otherTerm.value
            thisTerm.key.multiply(otherTerm.key) { basis, weight ->
                val finalWeight = weight*vectorWeight
                if(abs(finalWeight) > coeffLowerBound) result.plusAssign(basis, finalWeight)
            }
        }
    }
    return result
}


fun<AGENT> FockVector<AGENT>.multiplyAndStrip(other: FockVector<AGENT>): FockVector<AGENT> {
    return this.vectorMultiply(other, Basis<AGENT>::multiplyAndStrip)
}


operator fun<AGENT> CreationVector<AGENT>.div(basis: CreationBasis<AGENT>) : CreationVector<AGENT> {
    val result = HashDoubleVector<CreationBasis<AGENT>>()
    this.entries.forEach { result.plusAssign(it.key / basis, it.value) }
    return result
}


operator fun<BASIS> MutableDoubleVector<BASIS>.plusAssign(list: Collection<Pair<BASIS,Double>>) {
    list.forEach { this.plusAssign(it.first, it.second) }
}



fun<AGENT> Ground<AGENT>.integrate(hamiltonian: FockVector<AGENT>, T: Double, dt: Double, coeffLowerBound: Double = 1e-11) : CreationVector<AGENT> {
    val Hdt  = hamiltonian*dt
    val state =     Basis.identityCreationVector<AGENT>().toMutableVector()
    var time = 0.0
    while(time < T) {
//        println("Integrating: Time = $time state size = ${state.size}")
        state += Hdt.timesApproximate(state,coeffLowerBound) * this
        time += dt
    }
    return state
}


fun<AGENT> GroundedVector<AGENT,Ground<AGENT>>.integrate(hamiltonian: FockVector<AGENT>, T: Double, dt: Double, coeffLowerBound: Double = 1e-11) : CreationVector<AGENT> {
    val Hdt  = hamiltonian*dt
    val state = this.creationVector.toMutableVector()
    var time = 0.0
//    println("Hdt = $Hdt")
    while(time < T) {
        state += Hdt.timesApproximate(state,coeffLowerBound) * this.ground
        time += dt
//        println("t = $time state = $state")
    }
    return state
}


inline fun<AGENT, LHSBASIS, RHSBASIS: Basis<AGENT>, OUTBASIS: Basis<AGENT>>
        vectorMultiply(lhs: LHSBASIS, rhs: DoubleVector<RHSBASIS>, multiply: (LHSBASIS, RHSBASIS, (OUTBASIS, Double) -> Unit) -> Unit) : DoubleVector<OUTBASIS> {
    val result = HashDoubleVector<OUTBASIS>()
    rhs.entries.forEach { rhsTerm ->
        multiply(lhs, rhsTerm.key) { basis, weight ->
            result.plusAssign(basis, weight*rhsTerm.value)
        }
    }
    return result
}

inline fun<AGENT, LHSBASIS: Basis<AGENT>, RHSBASIS: Basis<AGENT>, OUTBASIS: Basis<AGENT>>
        DoubleVector<LHSBASIS>.vectorMultiply(rhs: DoubleVector<RHSBASIS>, multiply: (LHSBASIS, RHSBASIS, (OUTBASIS, Double) -> Unit) -> Unit) : DoubleVector<OUTBASIS> {
    val result = HashDoubleVector<OUTBASIS>()
    this.entries.forEach { lhsTerm ->
        rhs.entries.forEach { rhsTerm ->
            val termWeight = lhsTerm.value * rhsTerm.value
            multiply(lhsTerm.key, rhsTerm.key) { basis, weight ->
                result.plusAssign(basis, weight * termWeight)
            }
        }
    }
    return result
}


fun<AGENT,BASIS: Ground<AGENT>> CreationVector<AGENT>.asGroundedVector(ground: BASIS) = GroundedVector(this, ground)

// Calculates the highest order basis, b, such that no basis in this
// vector is of lower order than b
fun<AGENT> CreationVector<AGENT>.join() : CreationBasis<AGENT> {
    if(this.size == 0) throw(IllegalArgumentException("Join of an empty vector doesn't exist"))
    val it = this.iterator()
    val (firstBasis, _) = it.next()
    val join = HashMap<AGENT,Int>(firstBasis.creations)
    while(it.hasNext()) {
        val (thisBasis, _) = it.next()
        val joinIt = join.iterator()
        while(joinIt.hasNext()) {
            val entry = joinIt.next()
            val thisExponent = thisBasis.creations[entry.key]
            if(thisExponent != null) {
                entry.setValue(min(entry.value, thisExponent))
            } else {
                joinIt.remove()
            }
        }
    }
    return CreationBasis(join)
}

fun<AGENT> GroundedBasis<AGENT,DeselbyGround<AGENT>>.logProb(K: Map<AGENT,Int>): Double {
    var logProb = 0.0
    ground.lambdas.forEach { (agent, lambda) ->
        val ki = K[agent]?:0
        val Di = basis[agent]
        if(ki < Di) return Double.NEGATIVE_INFINITY
        val k = ki - Di
        if(lambda > 0.0) {
            logProb += k*ln(lambda) - lambda - Gamma.logGamma(k+1.0)
        } else {
            if(k != 0) return Double.NEGATIVE_INFINITY
        }
    }
    return logProb
}

//
//
//inline fun<AGENT, LHSBASIS: Basis<AGENT>, RHSBASIS: Basis<AGENT>, OUTBASIS: Basis<AGENT>>
//        DoubleVector<LHSBASIS>.multiply(creationVector: DoubleVector<RHSBASIS>, consumerTimes: (LHSBASIS, RHSBASIS, (OUTBASIS,Double) -> Unit) -> Unit): DoubleVector<OUTBASIS> {
//    val result = HashDoubleVector<OUTBASIS>()
//    this.entries.forEach { thisTerm ->
//        creationVector.entries.forEach {otherTerm ->
//            val vectorWeight = thisTerm.value * otherTerm.value
//            consumerTimes(thisTerm.key,otherTerm.key) { basis, weight ->
//                result.timesAssign(basis, weight * vectorWeight)
//            }
//        }
//    }
//    return result
//}





//operator fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.times(fockState :GroundedVector<AGENT, Ground<AGENT>>) : DoubleVector<CreationBasis<AGENT>> {
//    val result = HashDoubleVector<CreationBasis<AGENT>>()
//    this.entries.forEach { thisTerm ->
//        fockState.creationVector.forEach { otherTerm ->
//            val coeffProduct = thisTerm.value * otherTerm.value
//            thisTerm.key.multiplyTo(otherTerm.key, fockState.ground) { basis, weight ->
//                result.timesAssign(basis, weight*coeffProduct)
//            }
//        }
//    }
//    return result
//}
//
//fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.timesApproximate(fockState :GroundedVector<AGENT, Ground<AGENT>>, coeffLowerBound: Double) : DoubleVector<CreationBasis<AGENT>> {
//    val result = HashDoubleVector<CreationBasis<AGENT>>()
//    this.entries.forEach { thisTerm ->
//        fockState.creationVector.forEach { otherTerm ->
//            val coeffProduct = thisTerm.value * otherTerm.value
//            thisTerm.key.multiplyTo(otherTerm.key, fockState.ground) { basis, weight ->
//                val finalCoeffProduct = weight * coeffProduct
//                if(abs(finalCoeffProduct) > coeffLowerBound) result.timesAssign(basis, finalCoeffProduct)
//            }
//        }
//    }
//    return result
//}


//operator fun<AGENT> CovariantDoubleVector<Basis<AGENT>>.times(groundBasis: GroundedBasis<AGENT>) : DoubleVector<CreationBasis<AGENT>> {
//    val result = HashDoubleVector<CreationBasis<AGENT>>()
//    this.entries.forEach { thisTerm ->
//        thisTerm.key.multiplyTo(groundBasis) { basis, weight ->
//            result.timesAssign(basis, weight*thisTerm.value)
//        }
//    }
//    return result
//}


// Given a canonical operator, S, will create a mapping from
// agent states, d, to Operators such that d -> a^-_d[a*_d,S]
//
// Uses the identity
// [a*,a^nCreations] = -ma^(nCreations-1)
// so
// a^-_d[a*,a^nCreations] = -nCreations a^-_da^(nCreations-1)
//fun<AGENT> FockVector<AGENT>.toCreationCommutationMap(): CommutationMap<AGENT> {
//    val commutations = HashMap<AGENT, HashDoubleVector<Basis<AGENT>>>()
//    forEach { (basis, basisWeight) ->
//        basis.commutationsTo { d, basis, commutationWeight ->
//            commutations.getOrPut(d, { HashDoubleVector() }).
//                    timesAssign(basis.create(d,-1), basisWeight*commutationWeight)
//        }
//    }
//    return commutations
//}




//fun<AGENT> CommutationMap<AGENT>.semicommute(basis: CreationBasis<AGENT>): FockVector<AGENT> {
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