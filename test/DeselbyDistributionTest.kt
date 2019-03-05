import deselby.std.DeselbyDistribution
import org.junit.jupiter.api.Test

class DeselbyDistributionTest {
    @Test
    fun TestCreationAnnihilation() {
        val p = DeselbyDistribution(listOf(2.0,4.0))
        println(p.create(0).annihilate(0))
    }

    @Test
    fun TestSIRModel() {
        var p0 = DeselbyDistribution(listOf(20.0,40.0, 2.0))
        val ddt = SIRHamiltonian(p0)
        val d2dt2 = SIRHamiltonian(ddt)
        val d3dt3 = SIRHamiltonian(d2dt2)
        val d4dt4 = SIRHamiltonian(d3dt3)
        println(ddt)
        println(d2dt2)
        println(d3dt3)
        println(d4dt4)
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

}