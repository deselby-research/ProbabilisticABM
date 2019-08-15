package experiments.SIR

import deselby.fockSpace.Basis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.HashFockVector
import deselby.fockSpace.MutableFockVector
import deselby.fockSpace.extensions.commute
import deselby.fockSpace.extensions.toAnnihilationIndex
import deselby.std.vectorSpace.HashDoubleVector
import org.apache.commons.math3.random.MersenneTwister
import org.junit.Test
import java.util.function.Consumer

class TestSIR {

    @Test
    fun testCommutation() {
        val H = FockSIR.Hamiltonian()
        val index = H.toAnnihilationIndex()
        println(H)
        println(index.entries)
        println()

        index.commute(CreationBasis<Int>().create(0)) { basis, weight ->
            println("$weight $basis")
        }
        println()
        index.commute(CreationBasis<Int>().create(1)) { basis, weight ->
            println("$weight $basis")
        }

    }

    @Test
    fun testNonFockPosterior() {
        val observationInterval = 1.0
        val totalTime = 5.0
        val r = 0.9 // coeff of detection of infected
        val realStartState = NonFockSIR.SIRState(35, 5, 0)
        val params = NonFockSIR.SIRParams(0.01, 0.1, MersenneTwister())
        val observations = params.generateObservations(realStartState, observationInterval, r, totalTime)
        NonFockSIR.metropolisHastingsPosterior(observations, observationInterval, r)
    }

}
