import deselby.distributions.FockState
import deselby.distributions.discrete.DeselbyDistribution
import deselby.std.collections.Array2D
import deselby.std.collections.hashMultisetOf
import deselby.std.collections.iSize
import deselby.std.collections.toString2D
import experiments.spatialPredatorPrey.generatorPoly.Action
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random
import kotlin.streams.asStream
import kotlin.system.measureTimeMillis

class MyClass : Map<Int,Double> {
    override val entries: Set<Map.Entry<Int, Double>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val keys: Set<Int>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val size: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val values: Collection<Double>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun containsKey(key: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsValue(value: Double): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(key: Int): Double? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isEmpty(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

fun main() {
    val m = MyClass()
    println(m)
}
