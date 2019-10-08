package experiments.reverseSummation

import deselby.fockSpace.Basis
import deselby.fockSpace.DeselbyGround
import deselby.fockSpace.FockVector
import deselby.fockSpace.extensions.vectorMultiply
import kotlin.math.pow

class LgOperator<AGENT>(val observedAgents: Set<AGENT>, val r: Double) {
    // returns the commutation of this with basis, divided by this (i.e. to get the real commutation
    // right-multiply the result by this)
    fun commute(basis: Basis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        var diff = 0
        observedAgents.forEach { d ->
            val n = basis.creations[d] ?: 0
            val m = basis.annihilations[d] ?: 0
            diff += n - m
        }
        if(diff == 0) return
        termConsumer(basis, (1.0 - r).pow(diff) - 1.0)
    }

    fun commute(rhs: FockVector<AGENT>): FockVector<AGENT> {
        return vectorMultiply(this, rhs, LgOperator<AGENT>::commute)
    }

    operator fun times(D0: DeselbyGround<AGENT>): DeselbyGround<AGENT> {
        val notR = 1.0-r
        return DeselbyGround(D0.lambdas.mapValues { (d, lambdad) ->
            if(observedAgents.contains(d)) notR*lambdad else lambdad
        })
    }
}
