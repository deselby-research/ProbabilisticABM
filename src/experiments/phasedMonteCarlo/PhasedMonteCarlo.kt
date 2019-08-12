package experiments.phasedMonteCarlo

import deselby.distributions.discrete.DeselbyDistribution
import deselby.fockSpaceV2.Deselby
import deselby.fockSpaceV2.DeselbyPerturbation
import deselby.fockSpaceV2.FockBasisVector
import deselby.fockSpaceV2.Operator
import deselby.fockSpaceV2.extensions.*
import deselby.fockSpaceV1.*
import deselby.std.vectorSpace.*
import deselby.std.vectorSpace.extensions.integrate
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

class PhasedMonteCarlo {
    val lambda = 0.1
    val dt = 0.0001
    val T = 0.5
    lateinit var denseDeselby : DeselbyDistribution //= DeselbyDistribution(listOf(lambdas))
    lateinit var sparse : SparseFockState<Int>//(DeselbyBasis(mapOf(0 to lambdas)))
    lateinit var vector : DoubleVector<Deselby<Int>>//(DeselbyBasis(mapOf(0 to lambdas)))
    val H : (FockState<Int, MapFockState<Int>>) -> MapFockState<Int> = ::SparseH

    @Test
    fun testOperators() {
        reset()
        val sh = SparseH(sparse)
        val dh = DenseH(denseDeselby)
        val vh = VectorH(vector)
        println(dh)
        println(sh)
        println(vh)
        println(dh.annihilate(0))
        println(sh.annihilate(0))
        println(vh.annihilate(0))
    }

    @Test
    fun testCommutation() {
        reset()
        val hamiltonian = H(OperatorBasis.identity<Int>().toFockState())
        val commutations = CreationCommutations(hamiltonian)

        val vhamiltonian = VectorH(OneHotDoubleVector(Operator.identity<Int>(),1.0))
        val vcommutations = vhamiltonian.toCreationCommutationMap()

        val sampleAsPerturbation = DeselbyPerturbation(Deselby(mapOf(0 to lambda)))
        val sample = OneHotDoubleVector(sampleAsPerturbation,1.0)
        val vp = vhamiltonian * sample
        val possibleTransitionStates = SamplableDoubleVector(vhamiltonian * sample)


        println(sparse)
        println(sample)
        println(sampleAsPerturbation)
        println(hamiltonian * sparse)
        println(vhamiltonian * sample)

        println(possibleTransitionStates.coeffs.sum())

    }


    @Test
    fun testOperatorStates() {
        reset()
        val hamiltonian = H(OperatorBasis.identity<Int>().toFockState())
        val vhamiltonian = VectorH(OneHotDoubleVector(Operator.identity<Int>(),1.0))

        println(hamiltonian * sparse)
        println(vhamiltonian * vector)
    }



    @Test
    fun testDense() {
        reset()
        val exactIntegral = denseDeselby.integrate(::DenseH, T, dt)
        println(exactIntegral)

        val nSamples = 1000000
        var monteCarloSum = denseDeselby.monteCarloDiscrete(::DenseH, T, dt)
        var effectiveSamples = monteCarloSum.coeffs.asDoubleArray().sum()
        for(sample in 1 until nSamples) {
//        val s = p.monteCarloDiscrete(::SparseH,T,dt)
            val s = denseDeselby.monteCarloContinuous(::DenseH,T)
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
        println("p = $denseDeselby")
    }

    @Test
    fun compareIntegration() {
        reset()
        var deselbyResult : DeselbyDistribution? = null
        println(measureTimeMillis {
            val denseIntegral = denseDeselby.integrate(::DenseH, T, dt)
            deselbyResult = denseIntegral
        })
        println(deselbyResult)

        println()
        var vectorResult : DoubleVector<Deselby<Int>>? = null
        println(measureTimeMillis {
            val vectorIntegral = HashDoubleVector(vector)
            vectorIntegral.integrate(::VectorH, T, dt)
            vectorResult = vectorIntegral
        })
        println(vectorResult)

        println()
        var sparseResult : SparseFockState<Int>? = null
        println(measureTimeMillis {
            val sparseIntegral = SparseFockState(sparse)
            sparseIntegral.integrate(::SparseH, T, dt)
            sparseResult = sparseIntegral
        })
        println(sparseResult)

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
            val s = sparse.monteCarloContinuous(H,T)
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

    @Test
    fun perturbationTest() {
        reset()
        val exactIntegral = SparseFockState(sparse)
        exactIntegral.integrate(::SparseH, T, dt)
        println(exactIntegral)


        val nSamples = 10
        val monteCarloSum = SparseFockState<Int>()
        var effectiveSamples= 0.0
        for(sample in 1..nSamples) {
//            val s = sparse.perturbativeMonteCarlo(H,T)
            val s = sparse.perturbativeMonteCarlo(H,T)
            println(s)
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

    @Test
    fun vectorTest() {
        reset()
        val exactIntegral = HashDoubleVector(vector)
        exactIntegral.integrate(::VectorH, T, dt)
        println(exactIntegral)

        val nSamples = 1000000
        val monteCarloSum = HashDoubleVector<Deselby<Int>>()
        val initialState = OneHotDoubleVector(Deselby(mapOf(0 to 0.1)), 1.0)
        val hamiltonian = VectorH(OneHotDoubleVector(Operator.identity<Int>(),1.0))

        var effectiveSamples= 0.0
        for(sample in 1..nSamples) {
            val s = initialState.perturbativeMonteCarlo(hamiltonian,T)
            monteCarloSum += s
            effectiveSamples += s.coeff
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
        denseDeselby = DeselbyDistribution(listOf(lambda))
        sparse  = SparseFockState(DeselbyBasis(mapOf(0 to lambda)))
        vector = OneHotDoubleVector(Deselby(mapOf(0 to lambda)), 1.0)
    }

}

fun <S : FockState<Int, S>> SparseH(d: FockState<Int, S>): S {
    val a = d.annihilate(0).create(0)
    return a.create(0) - a
}

fun<BASIS : FockBasisVector<Int, BASIS>> VectorH(d: DoubleVector<BASIS>): DoubleVector<BASIS> {
    val a = d.annihilate(0).create(0)
    return a.create(0) - a
}


fun DenseH(d : deselby.distributions.FockState<Int,DeselbyDistribution>) : DeselbyDistribution {
    val a = d.annihilate(0).create(0)
    return a.create(0) - a
}
