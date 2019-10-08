package experiments.reverseSummation

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.extensions.times
import experiments.SIR.FockSIR
import experiments.SIR.SIRParams
import org.apache.commons.math3.distribution.PoissonDistribution
import org.junit.Test
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.test.assert

class SIRTests {
    val params = SIRParams(0.01, 0.1, 20.0, 7.0)
    val D0 = DeselbyGround(mapOf(0 to params.lambdaS, 1 to params.lambdaI))
    val H = FockSIR.Hamiltonian(params)
    val F0 = CreationBasis<Int>(emptyMap())

    // test e^(Ht) \approx (1 + H/n)^n
    @Test
    fun reverseMean() {
        val T = 1.0
        val n = 100
        val F1 = F0.asGroundedBasis(D0).integrate(H,T, T/n, 1e-11)
        println("Exact F1 = $F1")
        println("Exact F1 size = ${F1.size}")
        println("mean0 = ${F1.asGroundedVector(D0).mean(0)}")

        val aF1 = ActionBasis(emptyMap(), 0) * F1 * D0

        println("aF1 = $aF1")
        println("sum aF1 = ${aF1.values.sum()}")

        var fcm = ActionBasis(emptyMap(),0).toVector() as DoubleVector<Basis<Int>>
        val Fexpansion = HashDoubleVector(fcm)
        val hIndex = H.toCreationIndex()
        for(m in 1..15) {
            fcm = (T*(1.0-(m-1.0)/n)/m) * fcm.semicommute(hIndex)
            fcm = strip(fcm)
            Fexpansion += fcm
            println("F$m size = ${Fexpansion.size}")
            println("expansion sum = ${(Fexpansion * D0).values.sum()}")
        }
    }


    // test using e^(Ht) = e^(-t)e^((1 + H)t) = sum_n (e^(-t)t^n/n!)H^n for added stability and better convergence
    @Test
    fun priorTest() {
        val T = 1.0

        var fcm = ActionBasis(emptyMap(),0).toVector() * exp(-T)// as DoubleVector<Basis<Int>>
        val Fexpansion = HashDoubleVector(fcm)
        val hIndex = H.toCreationIndex()
        var sum = 0.0
        for(m in 1..15) {
            fcm = (T/m)*(fcm + fcm.semiCommuteAndStrip(hIndex))
            Fexpansion += fcm
            val reducedExpansion = Fexpansion * D0
            sum = reducedExpansion.values.sum()
            println("F$m size = ${Fexpansion.size}")
            println("expansion sum = $sum")
            println("groundedSum = ${(Fexpansion * F0.asGroundedBasis(D0)).values.sum()}")
            println("termsum = ${(fcm * F0.asGroundedBasis(D0)).values.sum()}")
            println("norm = ${reducedExpansion.normL1()}")
        }
        assert((sum - 18.59206120519).absoluteValue < 1e-10)
    }


    @Test
    fun reverseIntegrateAndSumTest() {
        val T = 1.0
        val observable = ActionBasis(emptyMap(),0).toVector()
        val sum = observable.reverseIntegrateAndSum(H.toCreationIndex(), T, F0.asGroundedBasis(D0), 1e-11)
        println("sum = $sum")
        assert((sum - 18.59206120519).absoluteValue < 1e-10)
    }

    @Test
    fun packagedPosteriorTest() {
        val T = 1.0
        val pObserve = 0.9
        val nObserved = 8

        val observable = Basis.newBasis(emptyMap(),mapOf(0 to 1)).toVector()
        val observation = BinomialBasis(pObserve, mapOf(1 to nObserved))
        val posterior = observable.reversePosteriorMean(
                H.toCreationIndex(),
                H.toAnnihilationIndex(),
                H,
                T,
                F0.asGroundedBasis(D0),
                observation,
                15
        )
        println("posterior = $posterior")
        assert((posterior - 18.39639407602808).absoluteValue < 1e-10)
    }

    @Test
    fun longhandPosteriorTest() {
        val T = 1.0
        val n = 500
        val pObserve = 0.9
        val nObserved = 8
        val F1 = F0.asGroundedBasis(D0).integrate(H,T, T/n, 1e-14)
        println("Exact F1 = $F1")
        println("Exact F1 size = ${F1.size}")
        val observation = BinomialLikelihood(1, pObserve, nObserved)
        val (posterior, posteriorD0) = observation * F1.asGroundedVector(D0)

        val aPosterior = ActionBasis(emptyMap(), 0) * posterior * posteriorD0
        val integralMean = aPosterior.values.sum()
        println("Integral posterior mean S = $integralMean")

        var fcm = Basis.newBasis(emptyMap(),mapOf(0 to 1, 1 to nObserved)).toVector()* exp(-T)// as DoubleVector<Basis<Int>>
        val LgcommuteH = LgOperator(setOf(1), pObserve).commute(H)
        val Fexpansion = HashDoubleVector(fcm)
        val hIndex = H.toCreationIndex()
        println("LgCommuteH = $LgcommuteH")
        var sum: Double = 0.0
        // [fLg,H] = f[Lg,H] + [f,H]Lg
        for(m in 1..15) {
            fcm = (T/m)*(fcm.semiCommuteAndStrip(hIndex) + fcm.multiplyAndStrip(LgcommuteH) + fcm)
            Fexpansion += fcm
            val reducedExpansion = Fexpansion * posteriorD0
            sum = reducedExpansion.values.sum()
            println("F$m size = ${Fexpansion.size}")
            println("expansion sum = $sum")
        }

        var norm = Basis.newBasis(emptyMap(),mapOf(1 to nObserved)).toVector()* exp(-T)// as DoubleVector<Basis<Int>>
        val normExpansion = HashDoubleVector(norm)
        var normSum: Double = 0.0
        for(m in 1..15) {
            norm = (T/m)*(norm.semiCommuteAndStrip(hIndex) + norm.multiplyAndStrip(LgcommuteH) + norm)
            normExpansion += norm
            val reducedExpansion = normExpansion * posteriorD0
            normSum = reducedExpansion.values.sum()
            println("F$m size = ${normExpansion.size}")
            println("expansion sum = $normSum")
        }
        println("Posterior = ${sum/normSum}")
        val difference = (sum/normSum)/integralMean
        println("ratio of forward and reverse integrals (should be 1.0) = $difference")
        assert((difference - 1.0).absoluteValue < 1e-4)
    }


    @Test
    fun independenceTest() {
        val D0 = DeselbyGround(mapOf(0 to 0.1, 1 to 0.2))
        val H =
                ActionBasis(emptySet(), 0).toVector() - ActionBasis(mapOf(0 to 1), 0).toVector() +
                        ActionBasis(emptySet(), 1).toVector() - ActionBasis(mapOf(1 to 1), 1).toVector()
        val a0 = ActionBasis(emptySet(), 0).toVector()
        val a1 = ActionBasis(emptySet(), 1).toVector()
        val F0 = CreationBasis<Int>(emptyMap())
        println("H = $H")

//        val mean0 = a0 * (H * D0) * D0
        val mean0 = a0 * D0

        println(mean0)
        println("mean0 = ${mean0.values.sum()}")

//        val mean1 = a1 * (H * D0) * D0
        val mean1 = a1 * D0

        println(mean1)
        println("mean1 = ${mean1.values.sum()}")

//        val mean10 = a1 * (a0 * (H * D0) * D0) * D0
        val mean10 = a1 * (a0 * D0) * D0

        println(mean10)
        println("mean 10 = ${mean10.values.sum()}")

        println("mean 10/mean1 = ${mean10.values.sum()/mean1.values.sum()}")
        println("mean 10/mean0 = ${mean10.values.sum()/mean0.values.sum()}")

    }


    fun<AGENT> strip(vector: DoubleVector<Basis<AGENT>>): DoubleVector<Basis<AGENT>> {
        val strippedVec = HashDoubleVector<Basis<AGENT>>()
        vector.forEach { (termBasis, termWeight) ->
            strippedVec.plusAssign(Basis.newBasis(emptyMap(), termBasis.annihilations), termWeight)
        }
        return strippedVec
    }
}