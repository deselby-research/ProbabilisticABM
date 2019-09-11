package experiments.SIR

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import experiments.phasedMonteCarlo.monteCarlo
import experiments.phasedMonteCarlo.monteCarloIntegrate
import experiments.phasedMonteCarlo.monteCarloIntegrateSingleThread

object FockSIR {

    fun posterior(observations : Array<Int>, observationInterval: Double, params: SIRParams, r: Double) {

        var D0 = DeselbyGround(mapOf(0 to 40.0, 1 to 7.0)) // initial prior
        var p : CreationVector<Int> = Basis.identityCreationVector<Int>()
        val hamiltonian = Hamiltonian(params)

        println(p)
        for(nObs in 0 until observations.size) {
            val binomial = BinomialLikelihood(1, r, observations[nObs])
            val state = binomial * p.asGroundedVector(D0)
            D0 = state.ground
            p = state.creationVector
            println("stats = ${D0.mean(p)}")
            if(nObs < observations.lastIndex) {
                p = p.asGroundedVector(D0).integrate(hamiltonian, observationInterval, 0.001, 1e-15)
            }
        }
//        println("stats = ${D0.mean(p)}")
    }

    fun prior(params: SIRParams, T: Double) {
        val D0 = DeselbyGround(mapOf(0 to params.lambdaS, 1 to params.lambdaI))
        var p : CreationVector<Int> = Basis.identityCreationVector()
        val hamiltonian = Hamiltonian(params)
        println(p)
        println("initial means = ${D0.mean(p).entries}")
        p = p.asGroundedVector(D0).integrate(hamiltonian, T, 0.001, 1e-10)
        println(p)
        println("normalisation = ${p.values.sum()}")
        println("means = ${D0.mean(p).entries}")
    }


    fun monteCarloPrior(params: SIRParams, T: Double, nSamples: Int) {
        val D0 = DeselbyGround(mapOf(0 to params.lambdaS, 1 to params.lambdaI))
        var sample = Basis.identity<Int>()
        val hamiltonian = Hamiltonian(params)
        val p = sample.asGroundedBasis(D0).monteCarloIntegrate(hamiltonian, T, nSamples)
        println(p)
        println("normalisation = ${p.values.sum()}")
        println("means = ${D0.mean(p)}")
    }


    fun monteCarloPosterior(observations : Array<Int>, observationInterval: Double, params: SIRParams, r: Double) {

        val D0 = DeselbyGround(mapOf(0 to 40.0, 1 to 7.0)) // initial prior
        val sample = Basis.identity<Int>()
        var posterior = sample.asGroundedBasis(D0)
        var forecast  = Basis.identityCreationVector<Int>()
        val hamiltonian = Hamiltonian(params)

        val nSamples = 1000000
        for(nObs in 0 until observations.size) {
            val observation = BinomialBasis(r, mapOf(1 to observations[nObs]))
            val renormalisedState = forecast / forecast.values.sum()
            posterior = observation.timesApproximate(renormalisedState.asGroundedVector(posterior.ground))
            println("stats = ${posterior.ground.mean(posterior.basis.toCreationVector())}")
            if(nObs < observations.lastIndex) {
                forecast = posterior.monteCarloIntegrate(hamiltonian, observationInterval, nSamples)
                println("forecast size = ${forecast.size}  sum = ${forecast.values.sum()}")
            }
        }
    }


    // H = beta(c_i^2  - c_s c_i)a_s a_i + gamma(1 - c_i)a_i
    //   = beta(c_1^2  - c_0 c_1)a_0 a_1 + gamma(1 - c_1)a_1
    //   = bc_1^2a_0 a_1  - bc_0 c_1a_0 a_1 + ga_1 - gc_1a_1
    fun Hamiltonian(params: SIRParams) : FockVector<Int> {
        val p = Basis.identityVector<Int>()
        val p0 = p.annihilate(1)
        val siSlector = (p0 * params.beta).annihilate(0)
        val infection2 = siSlector.create(0).create(1)
        val iSelector = p0 * params.gamma
        val recovery2 = iSelector.create(1)
        return siSlector.create(1).create(1) - infection2 + iSelector - recovery2
    }

    fun binomialProduct(m: Int, delta: Int, p: Double, lambda: Double) : CreationVector<Int> {
        val binom = BinomialLikelihood(0, p, m)
        val vec = Basis.identityCreationVector<Int>().create(0,delta)
        val D0 = DeselbyGround(mapOf(0 to lambda))
        val (state, _) = binom * vec.asGroundedVector(D0)
        return state
    }

    fun fallingFactorial(m: Int, delta: Int, lambda: Double) : CreationVector<Int> {
        val amamBasis = Basis.newBasis(mapOf(0 to m), mapOf(0 to m))
        val vec = Basis.identityCreationVector<Int>().create(0,delta)
        val D0 = DeselbyGround(mapOf(0 to lambda))
        return amamBasis * vec * D0
    }

}