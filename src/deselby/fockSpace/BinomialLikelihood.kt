package deselby.fockSpace

import deselby.fockSpace.extensions.multiply
import deselby.fockSpace.extensions.times
import deselby.std.abstractAlgebra.HasTimes
import deselby.std.vectorSpace.OneHotDoubleVector
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

    operator fun times(fock: FockState<AGENT,DeselbyGroundState<AGENT>>) =
        FockState(this * fock.creationVector * fock.ground, modifiedGroundState(fock.ground))


    // Implementation of identity
    // B_{p,m}(k) * D_{n,l}(k) = (1-p)^n\sum_q c_q D_{n+m-q,(1-p)l}
    // where
    // c_0 = l^m
    // c_{q+1} = (m-q)(n-q)/((q+1)l) c_q
    operator fun times(deselby: GroundBasis<AGENT,DeselbyGroundState<AGENT>>): Pair<CreationVector<AGENT>, DeselbyGroundState<AGENT>> {
        val perturbationState = HashCreationVector<AGENT>()
        val multiplier = (1.0-pObserve).pow(deselby.basis.creations[d]?:0)
        amamBasis.multiply(deselby) { basis, weight ->
            perturbationState.plusAssign(basis, weight * multiplier)
        }
        return Pair(perturbationState, modifiedGroundState(deselby.ground))
    }


    operator fun times(creationVector: CreationVector<AGENT>) : FockVector<AGENT> =
            multiply(this, creationVector, BinomialLikelihood<AGENT>::multiply)


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
