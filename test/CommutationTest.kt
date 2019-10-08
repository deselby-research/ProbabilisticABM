import deselby.fockSpace.Basis
import deselby.fockSpace.OperatorBasis
import org.junit.Test

class CommutationTest {
    @Test
    fun semiCommuteAndStripTest() {
        val a: OperatorBasis<Int> = Basis.newBasis(mapOf(1 to 1), mapOf(0 to 1, 1 to 1, 2 to 1)) as OperatorBasis<Int>
        val b = Basis.newBasis(mapOf(0 to 1), mapOf(3 to 1))
//        a0.semiCommuteAndStrip(c0a1) { basis, weight ->
//            println("$weight$basis")
//        }

        println("$a semicommute $b = ")
        a.semicommute(b) { basis, weight ->
            println("$weight$basis")
        }
    }
}