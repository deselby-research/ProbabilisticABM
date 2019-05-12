import deselby.std.DoubleNDArray
import deselby.std.fourierTransformInPlace

fun main(args : Array<String>) {

    val x = DoubleNDArray(listOf(4, 4, 2)) {1.2}

    x.fourierTransformInPlace(true)

    println(x)
//    println(measureTimeMillis {
//
//    })
}
