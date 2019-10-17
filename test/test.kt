import deselby.fockSpace.*
import deselby.std.Gnuplot
import deselby.std.combinatorics.combinations
import deselby.std.gnuplot
import deselby.std.step
import deselby.std.vectorSpace.HashDoubleVector
import experiments.spatialPredatorPrey.fock.Agent
import experiments.spatialPredatorPrey.fock.Predator
import experiments.spatialPredatorPrey.fock.Prey
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.linear.SparseFieldVector
import java.io.*
import java.util.*
import kotlin.math.floor

class MyClass(val c: Int) {
    override fun hashCode(): Int {
        return c
    }

    override fun equals(other: Any?): Boolean {
        if(other !is MyClass) return false
        return c == other.c
    }

    override fun toString(): String {
        return "c = $c"
    }
}

fun main(args : Array<String>) {
    val m = HashMap<Any,Any>()

    m[9] = 1234
    m["hello"] = "goodbye"
    m[MyClass(1234)] = MyClass(2345)

    println(m["hello"])
    println(m[9])
    println(m[1.0])
    println(m[MyClass(1234)])
}
