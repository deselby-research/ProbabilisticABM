import deselby.fockSpace.*
import deselby.std.Gnuplot
import deselby.std.combinatorics.combinations
import deselby.std.gnuplot
import deselby.std.step
import deselby.std.vectorSpace.HashDoubleVector
import experiments.spatialPredatorPrey.fock.Agent
import experiments.spatialPredatorPrey.fock.Predator
import experiments.spatialPredatorPrey.fock.Prey
import org.apache.commons.math3.linear.SparseFieldVector
import java.io.*
import java.util.*
import kotlin.math.floor


fun main(args : Array<String>) {
    val basis = CreationBasis(mapOf(Predator(1) as Agent to 1))
    val ground = DeselbyGround(mapOf(Predator(2) to 1.234, Prey(45) to 2.345))
    val binom = BinomialBasis(0.1234, mapOf(Predator(23) as Agent to 0))
    objWrite(binom)
    val read = objRead<BinomialBasis<Agent>>()
    println(read)
}

fun objWrite(obj: Any) {
    val expt = "Experiment.dat"
    val objStr = ObjectOutputStream(FileOutputStream(expt))
    objStr.writeObject(obj)
    objStr.close()
}

fun<T> objRead(): T {
    val fname = "Experiment.dat"
    val objStr = ObjectInputStream(FileInputStream(fname))
    val m = objStr.readObject() as T
    objStr.close()
    return m
}