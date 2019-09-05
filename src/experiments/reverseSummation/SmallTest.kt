package experiments.reverseSummation

import deselby.fockSpace.Basis
import deselby.fockSpace.DeselbyGround
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.extensions.times
import experiments.SIR.FockSIR
import experiments.SIR.SIRParams
import org.junit.Test

class SmallTest {

    // Calculate |a_i H F| where the norm is the sum of coefficients
    @Test
    fun proofOfConcept() {
        val D0 = DeselbyGround(mapOf(0 to 0.1, 1 to 0.2, 2 to 0.3))
        val params = SIRParams(0.01, 0.1, 40.0, 7.0)
        val H = FockSIR.Hamiltonian(params)
        val aH = H.annihilate(0)
        val a = Basis.newBasis(emptyMap(), mapOf(0 to 1))
        val commutation = a.commute(H)
        println("H = $H")
        println("aH = $aH")
        println("[a,H] = $commutation")
        println("Ha = ${H * a}")
        println("Ha + [a,H] = ${H * a + commutation}")
        println("HD0 = ${H * D0}")
        println("aHD0 = ${aH * D0}")
        println("[a,H]D0 = ${commutation * D0}")
        println()
        println("sum of H = ${H.values.sum()}")
        println("sum of HD0 = ${(H * D0).values.sum()}")
        println("sum of aH = ${aH.values.sum()}")
        println("sum of aHD0 = ${(aH * D0).values.sum()}")
        println("sum of [a,H]D0 = ${(commutation * D0).values.sum()}")

    }
}