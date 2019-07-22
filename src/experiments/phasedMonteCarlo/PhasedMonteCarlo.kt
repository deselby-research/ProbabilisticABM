package experiments.phasedMonteCarlo

import deselby.distributions.discrete.DeselbyDistribution
import experiments.fockBasis.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

class PhasedMonteCarlo {
    val lambda = 0.1
    val dt = 0.001
    val T = 0.5
    lateinit var deselby : DeselbyDistribution //= DeselbyDistribution(listOf(lambda))
    lateinit var sparse : SparseFockState<Int>//(DeselbyBasis(mapOf(0 to lambda)))

    @Test
    fun testSparseOperators() {
        reset()
        val sh = SparseH(sparse)
        val dh = DenseH(deselby)
        println(sh)
        println(dh)
        println(sh.annihilate(0))
        println(dh.annihilate(0))
    }

    @Test
    fun testDense() {
        reset()
        val exactIntegral = deselby.integrate(::DenseH, T, dt)
        println(exactIntegral)

        val nSamples = 1000000
        var monteCarloSum = deselby.monteCarloDiscrete(::DenseH, T, dt)
        var effectiveSamples = monteCarloSum.coeffs.asDoubleArray().sum()
        for(sample in 1 until nSamples) {
//        val s = p.monteCarloDiscrete(::SparseH,T,dt)
            val s = deselby.monteCarloContinuous(::DenseH,T)
            monteCarloSum += s
            effectiveSamples += s.coeffs.asDoubleArray().sum()
        }
        monteCarloSum = monteCarloSum * (1.0/effectiveSamples)
        println(monteCarloSum)
        println("Coefficient ratios")
        for(i in 0 until monteCarloSum.coeffs.size) {
            val r = monteCarloSum.coeffs.asDoubleArray()[i]/exactIntegral.coeffs.asDoubleArray()[i]
            print("%d=%.3f ".format(i,r))
        }
        println("")
        println("Coefficient SDs")
        val qabsSum = exactIntegral.coeffs.asDoubleArray().sumByDouble { abs(it) }
        val sampleabsSum = monteCarloSum.coeffs.asDoubleArray().sumByDouble { abs(it) }
        for(i in 0 until monteCarloSum.coeffs.size) {
            val qProb = abs(exactIntegral.coeffs.asDoubleArray()[i]) /qabsSum
            val sd = (abs(monteCarloSum.coeffs.asDoubleArray()[i]) /sampleabsSum - qProb)/ sqrt(qProb*(1.0-qProb)/effectiveSamples)
            print("%d=%.3f ".format(i,sd))
        }
        println("")
        println("absolute sum = $qabsSum")
        println("sample absolute sum = $sampleabsSum")
        println("Effective samples = $effectiveSamples")
        println("p = $deselby")
    }

    @Test
    fun compareSparseDenseIntegration() {
        reset()
        println(measureTimeMillis {
            val denseIntegral = deselby.integrate(::DenseH, T, dt)
            println(denseIntegral)
        })
        println(measureTimeMillis {
            val sparseIntegral = SparseFockState(sparse)
            sparseIntegral.integrate(::SparseH, T, dt)
            println(sparseIntegral)
        })
    }

    @Test
    fun sparseTest() {
        reset()
        val exactIntegral = SparseFockState(sparse)
        exactIntegral.integrate(::SparseH, T, dt)
        println(exactIntegral)

        val nSamples = 1000000
        val monteCarloSum = SparseFockState<Int>()
        var effectiveSamples= 0.0
        for(sample in 1..nSamples) {
            val s = sparse.monteCarloContinuous(::SparseH,T)
            monteCarloSum += s
            effectiveSamples += s.probability
        }
        monteCarloSum *= (1.0/effectiveSamples)
        println("MC sum = $monteCarloSum")
        println("Coefficient ratios")
        for(monomial in monteCarloSum.coeffs) {
            val r = (exactIntegral.coeffs[monomial.key]?:0.0)/monomial.value
            print("%d=%.3f ".format(monomial.key.count(0),r))
        }
        println("")
        println("Coefficient SDs")
        val qabsSum = exactIntegral.coeffs.values.sumByDouble(::abs)
        val sampleabsSum = monteCarloSum.coeffs.values.sumByDouble(::abs)
        for(monomial in monteCarloSum.coeffs) {
            val qProb = abs(exactIntegral.coeffs[monomial.key]?:0.0)/qabsSum
            val sd = (abs(monomial.value)/sampleabsSum - qProb)/sqrt(qProb*(1.0-qProb)/effectiveSamples)
            print("%d=%.3f ".format(monomial.key.count(0),sd))
        }
        println("")
        println("absolute sum = $qabsSum")
        println("sample absolute sum = $sampleabsSum")
        println("Effective samples = $effectiveSamples")
        println("p = $sparse")
    }

    fun reset() {
        deselby = DeselbyDistribution(listOf(lambda))
//        sparse  = SparseFockState(DeselbyBasis(mapOf(0 to lambda)))
        sparse  = SparseFockState(DeselbyBasis(mapOf(0 to lambda)))
    }

}

fun <S : FockState<Int,S>> SparseH(d: FockState<Int,S>): S {
    val a = d.annihilate(0).create(0)
    return a.create(0) - a
}


fun DenseH(d : deselby.distributions.FockState<Int,DeselbyDistribution>) : DeselbyDistribution {
    val a = d.annihilate(0).create(0)
    return a.create(0) - a
}
