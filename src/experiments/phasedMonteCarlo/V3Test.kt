package experiments.phasedMonteCarlo

import deselby.fockSpace.*
import deselby.fockSpace.extensions.annihilate
import deselby.fockSpace.extensions.create
import deselby.fockSpace.extensions.integrate
import deselby.std.gnuplot
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

class V3Test {
    val lambda = 0.1
    val dt = 0.0001
    val T = 2.0

    @Test
    fun absSize() {
        val H = hamiltonian()
        val deselbyGround = DeselbyGround(mapOf(0 to lambda))
        val data = ArrayList<Double>()
        for(T in 1..10) {
            val exactIntegral = deselbyGround.integrate(H, T*0.5, dt)
            val absSum = exactIntegral.values.sumByDouble(::abs)
            println(absSum)
            data.add(absSum)
        }

        gnuplot {
            val d = heredoc(data, 1)
            invoke("plot $d with lines")
        }
    }

    @Test
    fun monteCarloV3Test() {
        val H = hamiltonian()
        val deselbyGround = DeselbyGround(mapOf(0 to lambda))
        val exactIntegral = deselbyGround.integrate(H, T, dt)
        println(exactIntegral)

        val nSamples = 1000000
        val monteCarloSum = HashCreationVector<Int>()
        val initialState =  Basis.identity<Int>().asGroundedBasis(deselbyGround)
//        val hamiltonian = VectorH(OneHotDoubleVector(Operator.identity<Int>(),1.0))

        var effectiveSamples= 0.0
        val execTime = measureTimeMillis {
            for(sample in 1..nSamples) {
                val s = initialState.monteCarlo(H,T)
                monteCarloSum += s
                effectiveSamples += s.coeff
            }
        }
        println("Exec time = $execTime")
        monteCarloSum *= (1.0/effectiveSamples)
        println("MC sum = $monteCarloSum")
        println("Coefficient ratios")
        for(monomial in monteCarloSum) {
            val r = (exactIntegral[monomial.key]?:0.0)/monomial.value
            print("%d=%.3f ".format(monomial.key[0],r))
        }
        println("")
        println("Coefficient SDs")
        val qabsSum = exactIntegral.values.sumByDouble(::abs)
        val sampleabsSum = monteCarloSum.coeffs.values.sumByDouble(::abs)
        for(monomial in monteCarloSum.coeffs) {
            val qProb = abs(exactIntegral[monomial.key]?:0.0)/qabsSum
            val sd = (abs(monomial.value)/sampleabsSum - qProb)/sqrt(qProb*(1.0-qProb)/effectiveSamples)
            print("%d=%.3f ".format(monomial.key[0],sd))
        }
        println("")
        println("absolute sum = $qabsSum")
        println("sample absolute sum = $sampleabsSum")
        println("Effective samples = $effectiveSamples")
    }

    companion object {
        fun hamiltonian(): FockVector<Int> {
            val d = Basis.identityVector<Int>()
            val a = d.annihilate(0).create(0)
            return a.create(0) - a
        }

    }
}