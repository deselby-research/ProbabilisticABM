import deselby.distributions.discrete.DeselbyDistribution
import deselby.std.FallingFactorial
import org.junit.jupiter.api.Test

class DeselbyDistributionTest {
    @Test
    fun TestCreationAnnihilation() {
        val p = DeselbyDistribution(listOf(2.0, 4.0))
        val acp = p.create(0).annihilate(0)
        println(acp)
        assert(acp.toString() == "P[0, 0] + 2.0P[1, 0]")
    }

    @Test
    fun TestMultiplicationByFactorial() {
        val p = DeselbyDistribution(listOf(2.0, 4.0))
        println(p * FallingFactorial(0,2) * FallingFactorial(0,1))

    }

    @Test
    fun TestLambdaOptimisation() {
        val p = DeselbyDistribution(listOf(20.0, 40.0, 2.0))
        val dP = SIRHamiltonian(p)*0.001
        println(dP)
//        println(p.lambda)
        val pp = p.perturbWithLambda2(dP)
        println(pp)
        println(pp.lambda)
    }

    @Test
    fun TestSIRModel() {
        var p = DeselbyDistribution(listOf(20.0, 40.0, 2.0))
     //   println(SIRHamiltonian(p))
        println(p.dimension)
        println(p)
        for(t in 1..5) {
            p += SIRHamiltonian(p)*0.001
            p = p.truncateBelow(1e-4)
            println(p.dimension)
            println(p)
        }
//        println(p)
    }

    fun SIRHamiltonian(p : DeselbyDistribution) : DeselbyDistribution {
        val beta = 0.1
        val gamma = 0.2
        val p0 = p.annihilate(1)
        val infection = (p0 * beta).annihilate(0)
        val infection2 = infection.create(0).create(1)
        val recovery = p0 * gamma
        val recovery2 = recovery.create(1)
        return infection.create(1).create(1) - infection2 + recovery.create(2) - recovery2
    }

    @Test
    fun TestTruncation() {
        var p = DeselbyDistribution(listOf(20.0, 40.0, 0.001))
        val q = p.create(0).create(1).create(2).annihilate(0).annihilate(1).annihilate(2)
        println(q)
        println(q.truncateBelow(1.0))
    }

}