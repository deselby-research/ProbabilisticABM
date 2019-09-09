import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.*
import experiments.phasedMonteCarlo.monteCarlo
import org.junit.Test
import kotlin.system.measureTimeMillis

class FockSpaceVectorTest {


    @Test
    fun binomialBasisTest() {
        val D0 = DeselbyGround(mapOf(0 to 0.1, 1 to 0.2))
        val prior = CreationBasis(mapOf(0 to 2)).toCreationVector()*0.75 +
                CreationBasis(mapOf(0 to 2, 1 to 3)).toCreationVector() * 0.25
        val pObserve = 0.5
        val nObserve0 = 1
        val nObserve1 = 2
        val binom = BinomialBasis(pObserve, mapOf(0 to nObserve0, 1 to nObserve1))
        val L0 = BinomialLikelihood(0, pObserve, nObserve0)
        val L1 = BinomialLikelihood(1, pObserve, nObserve1)
        val (posterior, newGround) = L1 * (L0 * prior.asGroundedVector(D0))
        println(prior)
        println(posterior)
        println(newGround)
        println(newGround.mean(posterior))
        val reducedPosterior = binom.timesApproximate(prior.asGroundedVector(D0))
        println(reducedPosterior)
    }

    @Test
    fun testCommutation() {
        val H = ActionBasis(mapOf(1 to 1),0).toVector() -
                        ActionBasis(mapOf(0 to 1),0).toVector() +
                        ActionBasis(mapOf(0 to 1),1).toVector() -
                        ActionBasis(mapOf(1 to 1),1).toVector()
        val index = H.toAnnihilationIndex()
        val basis = CreationBasis(mapOf(0 to -1, 1 to 1))
        println("H = $H")
        println(index.entries)
        println("basis = $basis")
        println("[$H, $basis] = ${index.commute(basis)}")
    }


    @Test
    fun testMonteCarlo() {
        val D0 = DeselbyGround(mapOf(0 to 0.02, 1 to 0.02))
        val startState = CreationBasis<Int>(mapOf(0 to 1))
        val dieRate = 0.03
        val reproduceRate = 0.045
        val diffuseRate = 0.5
        val H =
//                (ActionBasis(mapOf(1 to 1),0).toVector() - ActionBasis(mapOf(0 to 1),0).toVector())*diffuseRate +
//                (ActionBasis(mapOf(0 to 1),1).toVector() - ActionBasis(mapOf(1 to 1),1).toVector())*diffuseRate
                (ActionBasis(emptyMap(),0).toVector() - ActionBasis(mapOf(0 to 1),0).toVector())*dieRate +
                (ActionBasis(mapOf(0 to 2),0).toVector() - ActionBasis(mapOf(0 to 1),0).toVector())*reproduceRate

        val hIndex = H.toAnnihilationIndex()
        val reducedHamiltonian = H * startState.asGroundedBasis(D0)
        println("D0 = $D0")
        println("startState = $startState")
        println("H = $H")
        println("reduced H = $reducedHamiltonian")
        println("hIndex = $hIndex")

        var p = startState.toCreationVector()
        p = p.asGroundedVector(D0).integrate(H, 20.0, 0.001, 1e-12)
        println("integral = $p")
        println()

        val total = HashCreationVector<Int>()
        val nSamples = 1000000
        var effectiveSamples = 0
        for(i in 1..nSamples) {
            val mcmcSample = startState.asGroundedBasis(D0).monteCarlo(hIndex, reducedHamiltonian, 20.0)
            total += mcmcSample
//            println(mcmcSample)
            if(mcmcSample.coeff < 0.0) effectiveSamples -= 1 else effectiveSamples += 1
        }
        val totalSum = total.values.sum()
        println("sum = ${totalSum/nSamples}")
        println("average of samples = ${total/totalSum}")
        println(effectiveSamples)
    }

    @Test
    fun testOperatorBasis() {
//    val ob = OperatorBasis(mapOf(0 to 2, 1 to 1), mapOf(0 to 3))
        val ob = OperatorBasis(emptyMap(), mapOf(0 to 2))
        val other = CreationBasis(mapOf(0 to 3))
        println(ob)
        println(other)
        ob.semicommute(other) { basis, weight ->
            println("$weight $basis")
        }
    }


    @Test
    fun testOperators() {
        val a  = OneHotDoubleVector(Basis.identity<Int>(),1.0)
        val b = a.annihilate(0)
        val c = a.create(0)
        println(a)
        println(b)
        println(c)
        println(Hamiltonian())
    }

    @Test
    fun testStuff() {
        val basis = CreationBasis(mapOf(0 to 1))
        val h = Basis.identityVector<Int>().annihilate(0)
        println(h)
        println(h * basis.asGroundedBasis(DeselbyGround(mapOf(0 to 0.1))))
    }

    @Test
    fun testIntegration() {
        val lambda = 0.1
        val dt = 0.0001
        val T = 0.5

        var opResult : DoubleVector<CreationBasis<Int>>? = null
        println(measureTimeMillis {
            val H  = Hamiltonian()*dt
            val ground = DeselbyGround(mapOf(0 to lambda))
            val state : MutableDoubleVector<CreationBasis<Int>> = HashDoubleVector(Basis.identity<Int>() to 1.0)

            println(H)
            var time = 0.0
            while(time < T) {
                state += H.timesApproximate(state, 1e-10) * ground
//                state += H * state * ground
                time += dt
            }
            opResult = state
        })
        println(opResult)
    }



    fun Hamiltonian(): DoubleVector<Basis<Int>> {
        val d = Basis.identityVector<Int>()
        val a = d.annihilate(0).create(0)
        return a.create(0) - a
    }


}