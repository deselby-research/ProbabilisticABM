import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.*
import org.junit.Test
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
    fun testCommutation() {
        val H = Hamiltonian(Basis.identityVector())
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
        println(Hamiltonian(a))
    }

    @Test
    fun testStuff() {
    }

    @Test
    fun testIntegration() {
        val lambda = 0.1
        val dt = 0.0001
        val T = 0.5

        var opResult : DoubleVector<CreationBasis<Int>>? = null
        println(measureTimeMillis {
            val H  = Hamiltonian(Basis.identity<Int>().toVector()*dt)
            val ground = DeselbyGroundState(0 to lambda)
            val state : MutableDoubleVector<CreationBasis<Int>> = HashDoubleVector(Basis.identity<Int>() to 1.0)

            println(H)
            var time = 0.0
            while(time < T) {
                state += H * (state on ground)
//                println()
//                println(state)
                time += dt
            }
            opResult = state
        })
        println(opResult)
    }



    fun Hamiltonian(d: CovariantDoubleVector<Basis<Int>>): DoubleVector<Basis<Int>> {
        val a = d.annihilate(0).create(0)
        return a.create(0) - a
    }


}