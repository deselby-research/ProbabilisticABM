package deselby.fockSpace

import deselby.fockSpace.extensions.join
import deselby.std.FallingFactorial
import deselby.std.extensions.fallingFactorial
import org.apache.commons.math3.distribution.BinomialDistribution
import org.apache.commons.math3.special.Gamma
import java.io.Serializable
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class BinomialBasis<AGENT>(val pObserve: Double, val observations: Map<AGENT,Int>): Serializable {
    val pNotObserve = 1.0 - pObserve

    // calculates the (unnormalised) monteCarloPrior probability of the observations
    // which is the product over i of ((1-pObserve)lambda_i)^m_i
    //
    fun times(D0: DeselbyGround<AGENT>): DeselbyGround<AGENT> {
        val newLambdas = HashMap<AGENT,Double>()
        D0.lambdas.mapValuesTo(newLambdas) { (_,lambda) ->
            pNotObserve * lambda
        }
        return DeselbyGround(newLambdas)
    }

    fun map(transform: (AGENT) -> AGENT): BinomialBasis<AGENT> {
        return BinomialBasis(pObserve, observations.mapKeys { transform(it.key) })
    }

    // finds the basis that minimises the KL divergence with this likelihood times prior
    fun timesApproximate(prior: GroundedVector<AGENT,DeselbyGround<AGENT>>): GroundedBasis<AGENT,DeselbyGround<AGENT>> {
//        val filteredMap = HashMap<CreationBasis<AGENT>,Double>()
//        monteCarloPrior.creationVector.filterTo(filteredMap) {abs(it.value) < 1e-5}
//        val filteredPrior = HashCreationVector(filteredMap)
        val cPrimes = reweight(prior)
        val basisFit = calcBasisFit(prior.creationVector)
        val newLambdas = HashMap<AGENT,Double>()
        prior.ground.lambdas.mapValuesTo(newLambdas) { (agent, lambda) ->
            val mj = observations[agent]?:0
            val lambdap = lambda*pNotObserve
            val newLambda = mj + lambdap - (basisFit.creations[agent]?:0) + meanSum(cPrimes, mj, lambdap, agent)
            if(newLambda < 0.0) println("Got -ve lambda: $newLambda")
            max(newLambda, 1e-8)
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


    // Calculates the log probability of concreteState given the observations in this
    // Where there is no observation, assume a prior poisson distribution given in 'priors'
    // uses the identity that B_r(k,m)D_0(k,lambda) \propto D_m(k,(1-r)lambda)
    // so P(k|m) = D_m(k,(1-r)lambda)
    fun logProb(concreteState: Map<AGENT,Int>, priors: Map<AGENT,Double>) : Double {
        var l = 0.0
        priors.forEach {(agent,lambda )->
            val kReal = concreteState[agent]?:0
            val kObserved  = observations[agent]
            if(kObserved != null) {
                if(kReal < kObserved) return Double.NEGATIVE_INFINITY
                val k = kReal - kObserved
                val lambdap = (1.0-pObserve)*lambda
                l += k*ln(lambdap) - lambdap - Gamma.logGamma(k+1.0)
            } else {
                l += kReal*ln(lambda) - lambda - Gamma.logGamma(kReal+1.0)
            }
        }
        return l
    }

    // Calculates the log likelihood of the observations in this
    // for the supplied 'concreteState', assuming that absent observations
    // are observed to be zero
    fun logLikelihood(concreteState: CreationBasis<AGENT>, allAgents: Iterable<AGENT>) : Double {
        var l = 0.0
//        val allAgents = observations.keys.times(concreteState.creations.keys)
        allAgents.forEach {agent ->
            l += BinomialDistribution(null, concreteState[agent], pObserve).logProbability(observations[agent]?:0) + ln(pObserve)
        }
        return l
    }


    // Calculates the log-probability of 'concreteState' on the
    // re-normalised product of this and 'prior'
    fun posteriorProbability(prior: GroundedVector<AGENT,DeselbyGround<AGENT>>, concreteState: CreationBasis<AGENT>): Double {
        var l = 0.0
        var normalisation = 0.0
        var prob = 0.0
        prior.creationVector.forEach { (priorBasis, priorWeight) ->
            var basisNormalisation = 1.0
            var basisProb = 1.0
            prior.ground.lambdas.forEach { (agent, lambda) ->
                val k = concreteState[agent]
                val m = observations[agent]?:0
                val delta = priorBasis[agent]
                val pm1d = pNotObserve.pow(delta)
                val lambdap = pNotObserve*lambda
                basisProb *= pm1d * k.fallingFactorial(m) * DeselbyGround.probability(k, delta, lambdap)
                basisNormalisation *= pm1d * binomialSum(m, delta, lambdap, lambdap.pow(m))
            }
            normalisation += priorWeight*basisNormalisation
            prob += priorWeight*basisProb
        }
        return prob / normalisation
    }

    // calculates the sum over k of B_m D_d,lambda
    fun binomialSum(m: Int, d: Int, lambda: Double, c0: Double = 1.0): Double {
        var c = c0
        var sum = c
        for(q in 1..min(d,m)) {
            val qm1 = q-1
            c *= (m-qm1)*(d-qm1)/(q*lambda)
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