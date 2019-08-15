package experiments.SIR

import deselby.fockSpace.*
import deselby.fockSpace.extensions.annihilate
import deselby.fockSpace.extensions.create
import deselby.fockSpace.extensions.on

object FockSIR {
    val beta = 0.01 // rate of infection per si pair
    val gamma = 0.1 // rate of recovery per person

    // H = beta(c_i^2  - c_s c_i)a_s a_i + gamma(1 - c_i)a_i
    //   = beta(c_1^2  - c_0 c_1)a_0 a_1 + gamma(1 - c_1)a_1
    //   = bc_1^2a_0 a_1  - bc_0 c_1a_0 a_1 + ga_1 - gc_1a_1
    fun Hamiltonian() : FockVector<Int> {
        val p = Basis.identityVector<Int>()
        val p0 = p.annihilate(1)
        val siSlector = (p0 * beta).annihilate(0)
        val infection2 = siSlector.create(0).create(1)
        val iSelector = p0 * gamma
        val recovery2 = iSelector.create(1)
        return siSlector.create(1).create(1) - infection2 + iSelector - recovery2
    }

    fun deselbyPosterior(observations : Array<Int>) {
        val observationInterval = 1.0
        val r = 0.9 // coeff of detection of infected

        val ground = DeselbyGroundState(mapOf(0 to 40.0, 1 to 7.0)) // initial prior
        val state = HashCreationVector<Int>()
//        var p = Basis.identityCreationVector<Int>() on ground
        val hamiltonian = Hamiltonian()
        for(nObs in 0 until observations.size) {
//            val state = p.integrate(hamiltonian, observationInterval, 0.001)
//            p = state.binomialObserve(r, observations[nObs], 1) //.truncateBelow(1e-5)
//            p.renormalise()
        }
        println(observations.asList())
//        println("Ibar = ${p.mean(1)}")
    }


}