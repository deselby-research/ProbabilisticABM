package models.SIR

import deselby.distributions.FockState
import deselby.distributions.discrete.DeselbyDistribution
import deselby.std.FallingFactorial

object DeselbySIR {
    fun posterior(observations: Array<Int>, observationInterval: Double, params: SIRParams, r: Double) {

        var p = DeselbyDistribution(listOf(params.lambdaS, params.lambdaI)) // initial monteCarloPrior
        for (nObs in 0 until observations.size) {
            p = p.binomialObserve(r, observations[nObs], 1) //.truncateBelow(1e-5)
            p.renormalise()
            p = p.truncateBelow(1e-10)
            println("Sbar = ${p.mean(0)} Ibar = ${p.mean(1)}")
            if (nObs < observations.lastIndex) {
                p = p.integrate({ SIRHamiltonian(params, it) }, observationInterval, 0.005)
//                p = p.integrateWithLambdaOptimisation({ SIRHamiltonian(params,it) }, observationInterval, 0.01)
            }
        }
        println("Sbar = ${p.mean(0)} Ibar = ${p.mean(1)}")
    }

    fun prior(params: SIRParams, T: Double) {
        var p = DeselbyDistribution(listOf(params.lambdaS, params.lambdaI)) // initial monteCarloPrior
        p = p.integrate({ SIRHamiltonian(params, it) }, T, 0.01)
        println(p)
        println("means = (${p.mean(0)}, ${p.mean(1)}")
    }

    fun <D : FockState<Int, D>> SIRHamiltonian(params: SIRParams, p: FockState<Int, D>): D {
        val p0 = p.annihilate(1)
        val siSlector = (p0 * params.beta).annihilate(0)
        val infection2 = siSlector.create(0).create(1)
        val iSelector = p0 * params.gamma
        val recovery2 = iSelector.create(1)
        return siSlector.create(1).create(1) - infection2 + iSelector - recovery2
    }


    fun binomialProductTest(m: Int, delta: Int, p: Double, lambda: Double): DeselbyDistribution {
        var vec = DeselbyDistribution(listOf(lambda))
        for(i in 1..delta) vec = vec.create(0)
        val prod = vec.binomialObserveTest(p, m, 0)
        prod.renormalise()
        return prod
    }

    fun binomialProduct(m: Int, delta: Int, p: Double, lambda: Double): DeselbyDistribution {
        var vec = DeselbyDistribution(listOf(lambda))
        for(i in 1..delta) vec = vec.create(0)
        val prod = vec.binomialObserve(p, m, 0)
        prod.renormalise()
        return prod
    }

    fun fallingFactorial(m: Int, delta: Int, lambda: Double) : DeselbyDistribution {
        var vec = DeselbyDistribution(listOf(lambda))
        for(i in 1..delta) vec = vec.create(0)
        return vec * FallingFactorial(0, m)
    }

}