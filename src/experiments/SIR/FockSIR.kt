package experiments.SIR

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.OneHotDoubleVector

object FockSIR {

    fun posterior(observations : Array<Int>, observationInterval: Double, params: SIRParams, r: Double) {

        var D0 = DeselbyGroundState(mapOf(0 to 40.0, 1 to 7.0)) // initial prior
        var p : CreationVector<Int> = Basis.identityCreationVector<Int>()
        val hamiltonian = Hamiltonian(params)

        println(p)
        for(nObs in 0 until observations.size) {
            val binomial = BinomialLikelihood(1, r, observations[nObs])
            val state = binomial * p(D0)
            D0 = state.ground
            p = state.creationVector
            println("stats = ${D0.mean(p)}")
            if(nObs < observations.lastIndex) {
                p = p(D0).integrate(hamiltonian, observationInterval, 0.001, 1e-12)
            }
        }
        println("stats = ${D0.mean(p)}")
    }

    fun prior(params: SIRParams, T: Double) {
        val D0 = DeselbyGroundState(mapOf(0 to params.lambdaS, 1 to params.lambdaI))
        var p : CreationVector<Int> = Basis.identityCreationVector()
        val hamiltonian = Hamiltonian(params)
        println(p)
        println("initial means = ${D0.mean(p).entries}")
        p = p(D0).integrate(hamiltonian, T, 0.001, 1e-10)
        println(p)
        println("normalisation = ${p.values.sum()}")
        println("means = ${D0.mean(p).entries}")
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
        val D0 = DeselbyGroundState(mapOf(0 to lambda))
        val (state, _) = binom * vec(D0)
        return state
    }

    fun fallingFactorial(m: Int, delta: Int, lambda: Double) : CreationVector<Int> {
        val amamBasis = Basis.newBasis(mapOf(0 to m), mapOf(0 to m))
        val vec = Basis.identityCreationVector<Int>().create(0,delta)
        val D0 = DeselbyGroundState(mapOf(0 to lambda))
        return amamBasis * vec * D0
    }

}