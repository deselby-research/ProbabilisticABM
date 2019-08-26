package experiments.phasedMonteCarlo

import deselby.distributions.FockState
import deselby.distributions.discrete.DeselbyDistribution
import kotlin.math.abs
import kotlin.math.sqrt

fun main() {
    val lambda = 0.1
    val dt = 0.001
    val T = 0.5
    val p = DeselbyDistribution(listOf(lambda))
    val q = p.integrate(::Hamiltonian, T, dt)
    println(q)


    val nSamples = 1000000
    var sum = p.monteCarloDiscrete(::Hamiltonian, T, dt)
    var effectiveSamples = sum.coeffs.asDoubleArray().sum()
    for(sample in 1 until nSamples) {
//        val s = p.monteCarloDiscrete(::SparseH,T,dt)
        val s = p.monteCarloContinuous(::Hamiltonian,T)
        sum += s
        effectiveSamples += s.coeffs.asDoubleArray().sum()
    }
    sum = sum * (1.0/effectiveSamples)
    println(sum)
    println("Coefficient ratios")
    for(i in 0 until sum.coeffs.size) {
        val r = sum.coeffs.asDoubleArray()[i]/q.coeffs.asDoubleArray()[i]
        print("%d=%.3f ".format(i,r))
    }
    println("")
    println("Coefficient SDs")
    val qabsSum = q.coeffs.asDoubleArray().sumByDouble { abs(it) }
    val sampleabsSum = sum.coeffs.asDoubleArray().sumByDouble { abs(it) }
    for(i in 0 until sum.coeffs.size) {
        val qProb = abs(q.coeffs.asDoubleArray()[i])/qabsSum
        val sd = (abs(sum.coeffs.asDoubleArray()[i])/sampleabsSum - qProb)/sqrt(qProb*(1.0-qProb)/effectiveSamples)
        print("%d=%.3f ".format(i,sd))
    }
    println("")
    println("absolute sum = $qabsSum")
    println("sample absolute sum = $sampleabsSum")
    println("Effective samples = $effectiveSamples")
    println("p = $p")
}


fun Hamiltonian(d : FockState<Int, DeselbyDistribution>) : DeselbyDistribution {
    val a = d.annihilate(0).create(0)
    return a.create(0) - a
}

//fun SparseH(d : GroundedVector<Int, DeselbyDistribution>) : DeselbyDistribution {
//    val a = d.create(0)
//    return a.create(0) - a
//}