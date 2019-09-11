package deselby.fockSpaceV2

import deselby.std.abstractAlgebra.HasTimes
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import java.lang.Math.pow
import kotlin.math.min

// Represents the likelihood of occupation number, nAnnihilations, given a probability
// of detection, p, and an observed number, nCreations.
// A probability of 0.0 and an observation of 0 is equivalent to no observation
//
// P(nAnnihilations|p,nCreations) \propto nAnnihilations!/((nAnnihilations-nCreations)!nCreations!) p^nCreations(1-p)^{nAnnihilations-nCreations}
//
// Only implementing pre-multiplication with Deselby for now
class BinomialLikelihood<AGENT>(val state : AGENT, val pObserve : Double, val nObserved : Int) :
        HasTimes<Deselby<AGENT>, DoubleVector<Deselby<AGENT>>> {

    // Implementation of identity
    // B_{p,nCreations}(k) * D_{d,l}(k) = (1-p)^d \sum_q c_qD_{d+nCreations-q,(1-r)l}
    // where
    // c_0 = l^nCreations
    // c_{q+1} = (nCreations-q)(d-q)/((q+1)l) c_q
    override fun times(multiplier: Deselby<AGENT>): DoubleVector<Deselby<AGENT>> {
        val result = HashDoubleVector<Deselby<AGENT>>()
        val delta = multiplier.creations[state]?:0
        val lambda = multiplier.lambda[state]?:0.0
        var cq = pow(lambda, nObserved.toDouble())
        val newLambda = HashMap(multiplier.lambda)
        newLambda[state] = lambda*(1.0-pObserve)
        var base = Deselby(newLambda, HashMap(multiplier.creations))
        base.createAssign(state, nObserved)
        for(q in 0 .. min(nObserved, delta)) {

            cq *= (nObserved - q)*(delta - q)/(lambda*(q+1))
        }
        return result
    }

}