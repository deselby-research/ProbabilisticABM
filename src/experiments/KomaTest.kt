package experiments

import koma.*
import koma.extensions.*
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.LUDecomposition
import java.util.*

fun main(args: Array<String>) {

    // Create some normal random noise
//    var a = randn(100,2)
//    var b = cumsum(a)

    var M = randn(3,3)
    var y = randn(3,1)

    var x = M.inv() * y


    println(y)
    println(M * x)

    val M2 = Array2DRowRealMatrix(Array(3, {i ->
        DoubleArray(3, { Random().nextDouble() })
    }) )
    val y2 = Array2DRowRealMatrix(Array(3, {i ->
        DoubleArray(1, { Random().nextDouble() })
    }) )

    val x2 = LUDecomposition(M2).solver.inverse.multiply(y2)

    println(y2)
    println(M2.multiply(x2))
}
