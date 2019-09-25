package experiments.reverseSummation

import deselby.fockSpace.Basis
import deselby.fockSpace.FockVector
import deselby.fockSpace.HashFockVector
import kotlin.math.pow

class GOperator<AGENT>(val d: AGENT, val r: Double, val lambda: Double) {

    // returns the commutation of this with basis, divided by this (i.e. to get the real commutation
    // right-multiply the result by this)
    fun commute(basis: Basis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val n = basis.creations[d]?:0
        val m = basis.annihilations[d]?:0
        var Al = (1.0-r).pow(n-m)
        if(Al != 1.0) termConsumer(basis, Al - 1.0)
        for(l in (m-1) downTo 0) {
            Al *= -r*lambda*l/(m-l+1)
            val alteredAnnihilations = HashMap<AGENT,Int>(basis.annihilations)
            if(l != 0) alteredAnnihilations[d] = l else alteredAnnihilations.remove(d)
            termConsumer(Basis.newBasis(basis.creations, alteredAnnihilations), Al)
        }
    }

    fun commute(vec: FockVector<AGENT>): FockVector<AGENT> {
        val commutation = HashFockVector<AGENT>()
        vec.forEach {(basis, weight) ->
            this.commute(basis) { commutationBasis, commutationWeight ->
                commutation.plusAssign(commutationBasis, weight * commutationWeight)
            }
        }
        return commutation
    }
}