package deselby.fockSpace

import deselby.fockSpace.extensions.vectorConsumerMultiply
import deselby.fockSpace.extensions.times
import kotlin.math.min
import kotlin.math.pow

// Represents the likelihood of occupation number, n, given a probability
// of detection, p, and an observed number, m.
// A probability of 0.0 and an observation of 0 is equivalent to no observation
//
// P(n|p,m) \propto n!/((n-m)!m!) p^m(1-p)^{n-m}
//
// Only implementing pre-multiplication with Deselby for now
//
// Implementation uses the strange fact that the coefficients of a Binomial, B, times
// a Deselby basis, D, are the same as
// B_m D_n = (1-p)^n a*^m a^m D_n
// this works for both Deselby and delta ground!
////////////////////////////////////////////////////
class BinomialLikelihood<AGENT>(val d : AGENT, val pObserve : Double, val nObserved : Int) {

    val amamBasis = Basis.newBasis(mapOf(d to nObserved), mapOf(d to nObserved))

    operator fun times(fock: FockState<AGENT,DeselbyGroundState<AGENT>>): FockState<AGENT, DeselbyGroundState<AGENT>> {
        val newGround = modifiedGroundState(fock.ground)
        val creationState = this * fock.creationVector * newGround
        val normalisedState = creationState / creationState.values.sum()
        return FockState(normalisedState, newGround)
    }

    // Implementation of identity
    // B_{p,m}(k) * D_{n,l}(k) = (1-p)^n\sum_q c_q D_{n+m-q,(1-p)l}
    // where
    // c_0 = l^m
    // c_{q+1} = (m-q)(n-q)/((q+1)l) c_q
    operator fun times(deselby: GroundBasis<AGENT,DeselbyGroundState<AGENT>>): Pair<CreationVector<AGENT>, DeselbyGroundState<AGENT>> {
        val perturbationState = HashCreationVector<AGENT>()
        val normalise = 1.0/basisSumOfWeights(deselby.basis.creations[d]?:0, deselby.ground.lambda(d))
        amamBasis.multiply(deselby) { b, w -> perturbationState.plusAssign(b, w * normalise) }
        return Pair(perturbationState, modifiedGroundState(deselby.ground))
    }


    operator fun times(creationVector: CreationVector<AGENT>) : FockVector<AGENT> =
            vectorConsumerMultiply(this, creationVector, BinomialLikelihood<AGENT>::multiply)


    private fun modifiedGroundState(g: DeselbyGroundState<AGENT>) : DeselbyGroundState<AGENT> {
        val newLambda = HashMap(g.lambdas)
        newLambda[d] = g.lambda(d)*(1.0-pObserve)
        return DeselbyGroundState(newLambda)
    }


    fun multiply(otherCreations: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val multiplier = (1.0-pObserve).pow(otherCreations[d])
        amamBasis.multiply(otherCreations) {basis , weight ->
            termConsumer(basis, weight*multiplier)
        }
    }

    // posterior weight for use during monte-carlo
    // n is the Deselby order in the d'th dimension
    fun sampleWeight(n: Int, lambda: Double) =
            (1.0-pObserve).pow(n) * basisSumOfWeights(n, lambda)


    fun basisSumOfWeights(n: Int, lambda: Double): Double {
        var sum = 0.0
        var c = lambda.pow(nObserved)
        for(q in 0..min(n,nObserved)) {
            sum += c
            c *= (nObserved-q)*(n-q)/((q+1)*lambda)
        }
        return sum
    }

//    data class Coefficient(val c: Double, val q: Int)
//
//    fun productCoefficients(n: Int, m: Int, lambda: Double) =
//            generateSequence(Coefficient((1.0-pObserve).pow(n)*lambda.pow(m), 0)) {
//                if(it.q == n || it.q == m) return@generateSequence null
//                val newq = it.q+1
//                Coefficient(it.c*(n-it.q)*(m-it.q)/(newq*lambda), newq)
//            }
//

}
