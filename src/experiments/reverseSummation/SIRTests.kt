package experiments.reverseSummation

import deselby.fockSpace.ActionBasis
import deselby.fockSpace.Basis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.DeselbyGround
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.extensions.times
import experiments.SIR.FockSIR
import experiments.SIR.SIRParams
import org.junit.Test

class SIRTests {
    val params = SIRParams(0.01, 0.1, 20.0, 7.0)
    val D0 = DeselbyGround(mapOf(0 to params.lambdaS, 1 to params.lambdaI))
    val H = FockSIR.Hamiltonian(params)
    val F0 = CreationBasis<Int>(emptyMap())


    @Test
    fun reverseMean() {
        val T = 1.0
        val n = 100
        val F1 = F0.asGroundedBasis(D0).integrate(H,T, T/n, 1e-11)
        println("Exact F1 = $F1")
        println("Exact F1 size = ${F1.size}")
        println("mean0 = ${F1.asGroundedVector(D0).mean(0)}")

        val aF1 = ActionBasis(emptyMap(), 0) * F1 * D0

        println("aF1 = $aF1")
        println("sum aF1 = ${aF1.values.sum()}")

        var fcm = ActionBasis(emptyMap(),0).toVector() as DoubleVector<Basis<Int>>
        val Fexpansion = HashDoubleVector(fcm)
        val hIndex = H.toCreationIndex()
        for(m in 1..15) {
            fcm = (T*(1.0-(m-1.0)/n)/m) * fcm.semicommute(hIndex)
            fcm = strip(fcm)
            Fexpansion += fcm
            println("F$m size = ${Fexpansion.size}")
            println("expansion sum = ${(Fexpansion * D0).values.sum()}")
        }
    }

    @Test
    fun otherTest() {

    }

    fun<AGENT> strip(vector: DoubleVector<Basis<AGENT>>): DoubleVector<Basis<AGENT>> {
        val strippedVec = HashDoubleVector<Basis<AGENT>>()
        vector.forEach { (termBasis, termWeight) ->
            strippedVec.plusAssign(Basis.newBasis(emptyMap(), termBasis.annihilations), termWeight)
        }
        return strippedVec
    }
}