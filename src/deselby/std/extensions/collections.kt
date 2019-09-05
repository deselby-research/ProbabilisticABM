package deselby.std.extensions

import kotlin.math.pow
import kotlin.math.sqrt

data class Statistics(val mean: Double, val variance: Double, val SD: Double)

fun Iterable<Number>.statistics() = this.asSequence().statistics()
fun Sequence<Number>.statistics() : Statistics {
    var sum = 0.0
    var sumOfSquares = 0.0
    var n = 0
    this.forEach {
        sum += it.toDouble()
        sumOfSquares += it.toDouble().pow(2)
        ++n
    }
    val mean = sum/n
    val variance = sumOfSquares/n - mean*mean
    return Statistics(mean, variance, sqrt(variance))
}


fun Iterable<Number>.variance() = this.asSequence().variance()
fun Sequence<Number>.variance() : Double {
    var sum = 0.0
    var sumOfSquares = 0.0
    var n = 0
    this.forEach {
        sum += it.toDouble()
        sumOfSquares += it.toDouble().pow(2)
        ++n
    }
    val mean = sum/n
    return sumOfSquares/n - mean*mean
}

fun Iterable<Number>.standardDeviation() = sqrt(this.asSequence().variance())
fun Sequence<Number>.standardDeviation() = sqrt(this.variance())


inline fun<T: Number> Iterable<T>.expectationValue(f : (T) -> Double) = this.asSequence().expectationValue(f)
inline fun<T: Number> Sequence<T>.expectationValue(f : (T) -> Double) : Double {
    var n = 0
    var sum = 0.0
    this.forEach {
        sum += f(it)
        ++n
    }
    return sum/n
}
