package experiments.fockBasis

import deselby.std.abstractAlgebra.MutableAlgebraElement

interface MutableFockState<AGENT, STATE : FockState<AGENT, STATE>> : FockState<AGENT,STATE>, MutableAlgebraElement<STATE, Double> {
    operator fun set(b : FockBasis<AGENT>, value : Double)

    fun integrate(hamiltonian : (FockState<AGENT,STATE>)-> STATE, T : Double, dt : Double) {
        var time = 0.0
        while(time < T) {
            this += hamiltonian(this)*dt
            time += dt
        }
    }

}