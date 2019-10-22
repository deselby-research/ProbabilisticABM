package experiments.reverseSummation

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.extensions.times
import models.SIR.FockSIR
import models.SIR.SIRParams
import org.junit.Test

class SmallTest {
    val params = SIRParams(0.01, 0.1, 20.0, 7.0)
    val D0 = DeselbyGround(mapOf(0 to params.lambdaS, 1 to params.lambdaI))
    val H = FockSIR.Hamiltonian(params)
    val F0 = CreationBasis<Int>(emptyMap())

    // Calculate |a_i H F| where the norm is the sum of coefficients



    @Test
    fun proofOfConcept() {
        val aH = H.annihilate(0)
        val a0 = Basis.newBasis(emptyMap(), mapOf(0 to 1))
        val commutation = a0.commute(H)

        println("H = $H")
        println("aH = $aH")
        println("[a,H] = $commutation")
        println("Ha = ${H * a0}")
        println("Ha + [a,H] = ${H * a0 + commutation}")
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


    @Test
    fun hamiltonianExpansion() {
        val T = 1.0
        val n = 100
        val F1 = F0.asGroundedBasis(D0).integrate(H,T, T/n, 1e-14)
        println("Exact F1 = $F1")

        var cm = F0.toCreationVector()
        val Fexpansion = HashDoubleVector(cm)
        for(m in 1..40) {
            cm = (T*(1.0-(m-1.0)/n)/m) * H * cm * D0
            Fexpansion += cm
            println("F$m = $Fexpansion")
            println("|cm| = ${cm.normL1()}")
            println("err = ${(Fexpansion - F1).normL1()}")
        }
    }
}