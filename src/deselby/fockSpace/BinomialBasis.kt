package deselby.fockSpace

import deselby.fockSpace.extensions.join
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class BinomialBasis<AGENT>(val pObserve: Double, val observations: Map<AGENT,Int>) {
    val pNotObserve = 1.0 - pObserve

    // calculates the (unnormalised) prior probability of the observations
    // which is the product over i of ((1-pObserve)lambda_i)^m_i
    //
    fun times(D0: DeselbyGround<AGENT>): DeselbyGround<AGENT> {
        val newLambdas = HashMap<AGENT,Double>()
        D0.lambdas.mapValuesTo(newLambdas) { (_,lambda) ->
            pNotObserve * lambda
        }
        return DeselbyGround(newLambdas)
    }


    fun timesApproximate(prior: GroundedVector<AGENT,DeselbyGround<AGENT>>): GroundedBasis<AGENT,DeselbyGround<AGENT>> {
        val cPrimes = reweight(prior)
        val basisFit = calcBasisFit(prior.creationVector)
        val newLambdas = HashMap<AGENT,Double>()
        prior.ground.lambdas.mapValuesTo(newLambdas) { (agent, lambda) ->
            val mj = observations[agent]?:0
            val lambdap = lambda*pNotObserve
            val newLambda = mj + lambdap - (basisFit.creations[agent]?:0) + meanSum(cPrimes, mj, lambdap, agent)
            if(newLambda < 0.0) println("Got -ve lambda: $newLambda")
            max(newLambda, 0.0)
        }
        return basisFit.asGroundedBasis(DeselbyGround(newLambdas))
    }


    private fun reweight(prior: GroundedVector<AGENT,DeselbyGround<AGENT>>): CreationVector<AGENT> {
        val reweighted = HashCreationVector<AGENT>()
        var sumOfWeights = 0.0
        prior.creationVector.mapValuesTo(reweighted) { (priorBasis, priorWeight) ->
            var newWeight = priorWeight
            priorBasis.creations.forEach { (agent, deltai) ->
                val mi = observations[agent]?:0
                val lambdap = prior.ground.lambda(agent)*pNotObserve
                newWeight *= pNotObserve.pow(deltai)
                newWeight *= binomialSum(mi, deltai, lambdap)
            }
            sumOfWeights += newWeight
            newWeight
        }
        reweighted *= 1.0/sumOfWeights
        return reweighted
    }


    private fun meanSum(cPrimes: CreationVector<AGENT>, mj: Int, lambdaj: Double, j: AGENT): Double {
        var sum = 0.0
        cPrimes.forEach {(basis, cPrime) ->
            val deltai = basis[j]
            sum += cPrime * (deltai - binomialFraction(mj, deltai, lambdaj))
        }
        return sum
    }

    private fun calcBasisFit(prior: CreationVector<AGENT>): CreationBasis<AGENT> {
        val minOrder = HashMap<AGENT,Int>(observations)
        prior.join().creations.forEach { (agent, order) ->
            minOrder.merge(agent, order) {a,b -> max(a,b)}
        }
        return CreationBasis(minOrder)
    }


    // calculates the sum over k of B_m D_n,lambda
    fun binomialSum(m: Int, n: Int, lambda: Double): Double {
        var c = 1.0 //lambda.pow(m)
        var sum = c
        for(q in 1..min(n,m)) {
            val qm1 = q-1
            c *= (m-qm1)*(n-qm1)/(q*lambda)
            sum += c
        }
        return sum
    }


    fun binomialFraction(m: Int,n: Int,lambda: Double): Double {
        var c = 1.0
        var sumq = 0.0
        var sum = c
        for(q in 1..min(n,m)) {
            val qm1 = q-1
            c *= (m-qm1)*(n-qm1)/(q*lambda)
            sum += c
            sumq += c*q
        }
        return sumq/sum
    }

    override fun toString(): String {
        return buildString {
            append("p=$pObserve ")
            append(observations.toString())
        }
    }

}