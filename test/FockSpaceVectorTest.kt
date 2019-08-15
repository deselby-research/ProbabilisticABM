import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.*
import org.junit.Test
import java.util.logging.Handler
import kotlin.system.measureTimeMillis

class FockSpaceVectorTest {
    @Test
    fun testBinomialMultiplication() {
//        val a = HashDoubleVector(BinomialLikelihood<Int>(emptyMap(),emptyMap()) to 1.0)
//        val b = HashDoubleVector(Deselby(mapOf(0 to 0.1)) to 1.0)
//
//        val c = a * b

    }

    @Test
    fun testOperatorBasis() {
//    val ob = OperatorBasis(mapOf(0 to 2, 1 to 1), mapOf(0 to 3))
        val ob = OperatorBasis(emptyMap(), mapOf(0 to 2))
        val other = CreationBasis(mapOf(0 to 3))
        println(ob)
        println(other)
        ob.commute(other) { basis, weight ->
            println("$weight $basis")
        }
    }

    @Test
    fun testCommutation() {
        val H = Hamiltonian()
        val index = H.toAnnihilationIndex()
        println(H)
        println(index.entries)
    }

    @Test
    fun testOperators() {
        val a  = OneHotDoubleVector(Basis.identity<Int>(),1.0)
        val b = a.annihilate(0)
        val c = a.create(0)
        println(a)
        println(b)
        println(c)
        println(Hamiltonian())
    }

    @Test
    fun testStuff() {
        val basis = CreationBasis(mapOf(0 to 1))
        val h = Basis.identityVector<Int>().annihilate(0)
        println(h)
        println(h * (basis on DeselbyGroundState(mapOf(0 to 0.1))))
    }

    @Test
    fun testIntegration() {
        val lambda = 0.1
        val dt = 0.0001
        val T = 0.5

        var opResult : DoubleVector<CreationBasis<Int>>? = null
        println(measureTimeMillis {
            val H  = Hamiltonian()*dt
            val ground = DeselbyGroundState(mapOf(0 to lambda))
            val state : MutableDoubleVector<CreationBasis<Int>> = HashDoubleVector(Basis.identity<Int>() to 1.0)

            println(H)
            var time = 0.0
            while(time < T) {
                state += H.timesApproximate(state, 1e-10) * ground
//                state += H * state * ground
                time += dt
            }
            opResult = state
        })
        println(opResult)
    }



    fun Hamiltonian(): DoubleVector<Basis<Int>> {
        val d = Basis.identityVector<Int>()
        val a = d.annihilate(0).create(0)
        return a.create(0) - a
    }


}