package experiments.SIR

import deselby.fockSpace.FockVector
import deselby.fockSpace.extensions.annihilate
import deselby.fockSpace.extensions.create

// H = beta(c_i^2  - c_s c_i)a_s a_i + gamma(1 - c_i)a_i
//   = beta(c_1^2  - c_0 c_1)a_0 a_1 + gamma(1 - c_1)a_1
//   = bc_1^2a_0 a_1  - bc_0 c_1a_0 a_1 + ga_1 - gc_1a_1
fun SIRHamiltonian(p : FockVector<Int>) : FockVector<Int> {
    val beta = 0.01 // rate of infection per si pair
    val gamma = 0.1 // rate of recovery per person
    val p0 = p.annihilate(1)
    val siSlector = (p0 * beta).annihilate(0)
    val infection2 = siSlector.create(0).create(1)
    val iSelector = p0 * gamma
    val recovery2 = iSelector.create(1)


    return siSlector.create(1).create(1) - infection2 + iSelector - recovery2
}
