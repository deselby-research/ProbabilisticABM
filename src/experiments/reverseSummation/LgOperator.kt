package experiments.reverseSummation

import deselby.fockSpace.Basis
import deselby.fockSpace.FockVector
import deselby.fockSpace.extensions.vectorMultiply
import kotlin.math.pow

class LgOperator<AGENT>(val d: AGENT, val r: Double) {
    // returns the commutation of this with basis, divided by this (i.e. to get the real commutation
    // right-multiply the result by this)
    fun commute(basis: Basis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val n = basis.creations[d] ?: 0
        val m = basis.annihilations[d] ?: 0
        var weight = (1.0 - r).pow(n - m) - 1.0
        termConsumer(Basis.newBasis(basis.creations, basis.annihilations), weight)
    }

    fun commute(rhs: FockVector<AGENT>): FockVector<AGENT> {
        return vectorMultiply(this, rhs, LgOperator<AGENT>::commute)
    }
}
