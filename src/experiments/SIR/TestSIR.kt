package experiments.SIR

import deselby.fockSpace.Basis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.HashFockVector
import deselby.fockSpace.MutableFockVector
import deselby.fockSpace.extensions.commute
import deselby.fockSpace.extensions.toAnnihilationIndex
import deselby.std.vectorSpace.HashDoubleVector
import org.junit.Test
import java.util.function.Consumer

class TestSIR {

    @Test
    fun testCommutation() {
        val H = SIRHamiltonian(Basis.identityVector())
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
}
