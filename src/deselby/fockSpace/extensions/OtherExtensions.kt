package deselby.fockSpace.extensions

import deselby.fockSpace.*

fun<AGENT> CreationIndex<AGENT>.allTermsContaining(agents: Iterable<AGENT>): Set<Map.Entry<Basis<AGENT>,Double>> {
    val activeTerms = HashSet<Map.Entry<Basis<AGENT>,Double>>()
    agents.forEach { d ->
        this[d]?.forEach { term ->
            activeTerms.add(term)
        }
    }
    return activeTerms
}


inline fun<AGENT> AnnihilationIndex<AGENT>.commute(otherBasis: CreationBasis<AGENT>, crossinline termConsumer: (Basis<AGENT>, Double) -> Unit) {
    this.allTermsContaining(otherBasis.creations.keys).forEach { (indexedBasis, indexedWeight) ->
        indexedBasis.semicommute(otherBasis) { commutedBasis, commutedWeight ->
            termConsumer(commutedBasis, commutedWeight * indexedWeight)
        }
    }
}


fun<AGENT> AnnihilationIndex<AGENT>.commute(otherBasis: CreationBasis<AGENT>) : FockVector<AGENT> {
    val commutation = HashFockVector<AGENT>()
    this.commute(otherBasis, commutation::plusAssign)
    return commutation
}


fun<AGENT> GroundedVector<AGENT,DeselbyGround<AGENT>>.means(): Map<AGENT,Double> {
    return ground.lambdas.keys.associateWith(this::mean)
}

fun<AGENT> GroundedVector<AGENT,DeselbyGround<AGENT>>.mean(d: AGENT): Double {
    val lambdad = ground.lambdas[d]?:0.0
    return creationVector.entries.sumByDouble { (basis, weight) ->
        weight * (lambdad + basis[d])
    }
}

fun<AGENT> GroundedBasis<AGENT,DeselbyGround<AGENT>>.means(): Map<AGENT,Double> {
    return ground.lambdas.keys.associateWith(this::mean)
}

fun<AGENT> GroundedBasis<AGENT,DeselbyGround<AGENT>>.mean(d: AGENT): Double {
    return ground.lambdas[d]?:0.0 + basis[d]
}