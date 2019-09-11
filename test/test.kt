import deselby.fockSpace.Basis
import deselby.fockSpace.BinomialLikelihood
import deselby.fockSpace.CommutationSequence
import deselby.fockSpace.OperatorBasis
import deselby.std.Gnuplot
import deselby.std.combinatorics.combinations
import deselby.std.gnuplot
import deselby.std.step
import deselby.std.vectorSpace.HashDoubleVector
import java.util.*
import kotlin.math.floor


fun main() {
//    val a = OperatorBasis(mapOf(0 to 1), mapOf(0 to 3, 1 to 1))
//    val b = OperatorBasis(mapOf(0 to 2, 1 to 1), mapOf(0 to 3))

    val a = OperatorBasis(mapOf(3 to 1), mapOf(0 to 3, 1 to 1))
    val b = OperatorBasis(mapOf(0 to 2, 1 to 1), mapOf(3 to 1))

    val commutation = HashDoubleVector<Basis<Int>>()
    a.semicommute(b, commutation::plusAssign)
    println(commutation)
}
