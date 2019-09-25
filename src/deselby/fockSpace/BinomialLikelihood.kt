package deselby.fockSpace

import deselby.fockSpace.extensions.vectorMultiply
import deselby.fockSpace.extensions.times
import kotlin.math.min
import kotlin.math.pow

// Represents the likelihood of occupation number, n, given a probability
// of detection, pObserve, and an observed number, nObserved.
// A pObserve of 0.0 and an observation of 0 is equivalent to no observation
//
// P(n|p,nObserved) \propto n!/((n-nObserved)!nObserved!) p^nObserved(1-p)^{n-nObserved}
//
// Only implementing pre-multiplication with Deselby for now
//
// Implementation uses the strange fact that the coefficients of a Binomial, B, times
// a Deselby basis, D, are, when expressed as Deselby bases, the same as
// B_m D_n = (1-p)^n a*^nObservations a^nObservations D_n
// this works for both Deselby and delta ground!
////////////////////////////////////////////////////
class BinomialLikelihood<AGENT>(val d : AGENT, val pObserve : Double, val nObserved : Int) {

    val amamBasis = Basis.newBasis(mapOf(d to nObserved), mapOf(d to nObserved))

    // returns the posterior for the given prior
    operator fun times(prior: GroundedVector<AGENT,DeselbyGround<AGENT>>): GroundedVector<AGENT, DeselbyGround<AGENT>> {
        val newGround = modifiedGroundState(prior.ground)
        val creationState = this * prior.creationVector * newGround
        val normalisedState = creationState / creationState.values.sum()
        return GroundedVector(normalisedState, newGround)
    }

    // Implementation of identity
    // B_{p,nObserved}(k) * D_{n,l}(k) = (1-p)^n\sum_q c_q D_{n+nObserved-q,(1-p)l}
    // where
    // c_0 = l^nObserved
    // c_{q+1} = (nObserved-q)(n-q)/((q+1)l) c_q
    operator fun times(deselby: GroundedBasis<AGENT,DeselbyGround<AGENT>>): GroundedVector<AGENT, DeselbyGround<AGENT>> {
        val perturbationState = HashCreationVector<AGENT>()
        val newGround = modifiedGroundState(deselby.ground)
        val normalise = 1.0/basisSumOfWeights(deselby.basis.creations[d]?:0, deselby.ground.lambda(d))
        amamBasis.multiply(deselby) { b, w -> perturbationState.plusAssign(b, w * normalise) }
        return GroundedVector(perturbationState, newGround)
    }


    operator fun times(prior: CreationVector<AGENT>) : FockVector<AGENT> =
            vectorMultiply(this, prior, BinomialLikelihood<AGENT>::multiply)


    private fun modifiedGroundState(g: DeselbyGround<AGENT>) : DeselbyGround<AGENT> {
        val newLambda = HashMap(g.lambdas)
        newLambda[d] = g.lambda(d)*(1.0-pObserve)
        return DeselbyGround(newLambda)
    }

    // returns a FockVector that, when grounded on a modified ground state (i.e. updated lambdas),
    // gives the (un-normalised) posterior
    fun multiply(prior: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val multiplier = (1.0-pObserve).pow(prior[d])
        amamBasis.multiply(prior) { basis, weight ->
            termConsumer(basis, weight*multiplier)
        }
    }

    // posterior weight for use during monte-carlo
    // nAnnihilations is the Deselby order in the d'th dimension
    fun sampleWeight(deselby: GroundedBasis<AGENT,DeselbyGround<AGENT>>): Double {
        val n = deselby.basis.creations[d]?:0
        return (1.0 - pObserve).pow(n) * basisSumOfWeights(n, deselby.ground.lambda(d))
    }


    fun basisSumOfWeights(n: Int, lambda: Double): Double {
        var c = lambda.pow(nObserved)
        var sum = c
        for(q in 1..min(n,nObserved)) {
            val qm1 = q - 1
            c *= (nObserved-qm1)*(n-qm1)/(q*lambda)
            sum += c
        }
        return sum
    }

}
