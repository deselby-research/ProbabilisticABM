import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.*
import org.junit.Test
import java.util.AbstractMap
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
    fun testOperators() {
        val a  = OneHotDoubleVector(ActBasis.identity<Int>(),1.0)
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

        var opResult : DoubleVector<ActCreationBasis<Int>>? = null
        println(measureTimeMillis {
            val H  = Hamiltonian(ActBasis.identity<Int>().toVector()*dt)
            val ground = DeselbyGroundState(0 to lambda)
            val state : MutableDoubleVector<ActCreationBasis<Int>> = HashDoubleVector(ActBasis.identity<Int>() to 1.0)

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



    fun Hamiltonian(d: CovariantDoubleVector<ActBasis<Int>>): DoubleVector<ActBasis<Int>> {
        val a = d.annihilate(0).create(0)
        return a.create(0) - a
    }


}