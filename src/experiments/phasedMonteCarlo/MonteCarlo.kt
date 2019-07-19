package experiments.phasedMonteCarlo

import deselby.distributions.FockState
import deselby.distributions.discrete.DeselbyDistribution
import deselby.std.collections.DoubleNDArray
import deselby.std.distributions.MutableCategorical
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sign
import kotlin.random.Random

fun DeselbyDistribution.sample() {
    val probs = coeffs.asDoubleArray()
    val choose = MutableCategorical<Int>(probs.size)
    choose.createBinaryTree(0 until probs.size, coeffs.asDoubleArray().asSequence().map {abs(it)}.asIterable())
    val index = choose.sample()
    val phase = probs[index].sign
    setOneHot(coeffs.toNDIndex(index), phase)
}

fun DeselbyDistribution.sampleIndex() : IntArray {
    val choose = MutableCategorical<Int>()
    choose.createBinaryTree(0 until coeffs.size, coeffs.asDoubleArray().asSequence().map {abs(it)}.asIterable())
    val index = choose.sample()
    return coeffs.toNDIndex(index)
}

fun DeselbyDistribution.setOneHot(ndIndex : IntArray, value : Double) {
    val newShape = IntArray(ndIndex.size) { ndIndex[it]+1 }
    coeffs = DoubleNDArray(newShape.asList()) { 0.0 }
    coeffs[ndIndex] = value
}


fun DeselbyDistribution.monteCarloDiscrete(H: (FockState<Int, DeselbyDistribution>) -> DeselbyDistribution, T : Double, dt : Double) : DeselbyDistribution {
    val p = DeselbyDistribution(this)
    var time = 0.0
    var sampleProbs = p + H(p)*dt
    var sampleWeight = 1.0
    val choose = MutableCategorical<Int>(sampleProbs.coeffs.size)
    choose.createBinaryTree(0 until sampleProbs.coeffs.size, sampleProbs.coeffs.asDoubleArray().asSequence().map {abs(it)}.asIterable())
    while(time < T) {
        val sampleNDIndex = sampleProbs.coeffs.toNDIndex(choose.sample())
        sampleWeight *= choose.sum()
        if(p.coeffs.getOrElse(sampleNDIndex){ 0.0 } == 0.0) {
            p.setOneHot(sampleNDIndex, sampleProbs.coeffs[sampleNDIndex].sign)
//            println("event ${choose.sum()}")
            sampleProbs = (p + H(p) * dt)
            choose.createBinaryTree(0 until sampleProbs.coeffs.size, sampleProbs.coeffs.asDoubleArray().asSequence().map { abs(it) }.asIterable())
        }
        time += dt
    }
    println(sampleWeight)
    return p * sampleWeight
}


fun DeselbyDistribution.monteCarloContinuous(H: (FockState<Int, DeselbyDistribution>) -> DeselbyDistribution, T : Double) : DeselbyDistribution {
    val p = DeselbyDistribution(this)
    var time = 0.0
    val choose = MutableCategorical<Int>()
    var sampleWeight = 1.0
    var sampleIndex = coeffs.toNDIndex(coeffs.asDoubleArray().indexOfFirst { it != 0.0 })
    var samplePhase = coeffs[sampleIndex].sign
    while(time < T) {
        p.setOneHot(sampleIndex, samplePhase)
        val dp_dt = H(p)
        choose.createBinaryTree(0 until dp_dt.coeffs.size, dp_dt.coeffs.asDoubleArray().asSequence().map {abs(it)}.asIterable())
        choose.remove(dp_dt.coeffs.toFlatIndex(sampleIndex)!!)
        val weightGrowthRate = choose.sum() + dp_dt.coeffs[sampleIndex]*samplePhase
        var timeToNextEvent = -ln(1.0 - Random.nextDouble()) / choose.sum() // sum is rate of events
        if(time + timeToNextEvent >= T) timeToNextEvent = T - time
        time += timeToNextEvent
        sampleWeight *= exp(weightGrowthRate*timeToNextEvent)
        val chosenIndex = choose.sample()
        sampleIndex = dp_dt.coeffs.toNDIndex(chosenIndex)
        samplePhase = dp_dt.coeffs.asDoubleArray()[chosenIndex].sign
    }
    return p * sampleWeight
}


//fun DeselbyDistribution.monteCarloDiscrete(SparseH: (FockState<Int, DeselbyDistribution>) -> DeselbyDistribution, T : Double, dt : Double) : DeselbyDistribution {
//    var p = this
//    var time = 0.0
//    while(time < T) {
//        p = (p + SparseH(p)*dt)
//        val absSum = p.coeffs.asDoubleArray().sumByDouble { abs(it) }
//        p.sample()
//        p = p * absSum
//        time += dt
//    }
//    return p
//}
