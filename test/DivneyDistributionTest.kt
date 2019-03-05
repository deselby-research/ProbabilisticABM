import deselby.std.DivneyDistribution
import org.junit.jupiter.api.Test

public class DivneyDistributionTest {
    @Test
    fun TestCreationAnnihilation() {
        val p = DivneyDistribution(listOf(2.0,4.0))
        p.annihilate(0)
        p.annihilate(1)
        p.create(0)
        p.create(0)
        p.create(0)
        println(p)
    }

    @Test
    fun TestSIRModel() {
        var p0 = DivneyDistribution(listOf(20.0,40.0, 2.0))
        val ddt = SIRHamiltonian(p0)
        val d2dt2 = SIRHamiltonian(ddt)
        val d3dt3 = SIRHamiltonian(d2dt2)
        val d4dt4 = SIRHamiltonian(d3dt3)
        println(ddt.numberOfCoeffsAbove(1e-6))
        println(d2dt2.numberOfCoeffsAbove(1e-6))
        println(d3dt3.numberOfCoeffsAbove(1e-6))
        println(d4dt4.numberOfCoeffsAbove(1e-6))
    }

    fun SIRHamiltonian(p : DivneyDistribution) : DivneyDistribution {
        val beta = 0.1
        val gamma = 0.2
        val p0 = p.copyOf().annihilate(1)
        val infection = (p0 * beta).annihilate(0)
        val infection2 = infection.copyOf().create(0).create(1)
        val recovery = p0 * gamma
        val recovery2 = recovery.copyOf().create(1)
        return infection.create(1).create(1) - infection2 + recovery.create(2) - recovery2
    }
}
