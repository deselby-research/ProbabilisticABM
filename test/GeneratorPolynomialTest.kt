import deselby.distributions.discrete.GeneratorPolynomial
import org.junit.jupiter.api.Test

class GeneratorPolynomialTest {
    @Test
    fun TestCreationAnnihilation() {
        val p = GeneratorPolynomial()
        val acp = p.create(0).create(0).create(1).annihilate(0)
        println(acp)
        assert(acp.size == 1)
        assert(acp[listOf(1,1)] == 2.0)
    }

    @Test
    fun TestArithmetic() {
        val p = GeneratorPolynomial()
        val acp = p.create(0) + p.create(0) - p.create(1)
        println(acp)
//        assert(acp.size == 1)
//        assert(acp[listOf(1,1)] == 2.0)
    }

    @Test
    fun TestSIRModel() {
        var p = GeneratorPolynomial()
        p = p.create(0, 40).create(1, 20)
        println(SIRHamiltonian(p))
        println(p)
//        for(t in 1..5) {
//            p += SIRHamiltonian(p)*0.001
//            println(p)
//        }
//        println(p)
    }

    fun SIRHamiltonian(p : GeneratorPolynomial) : GeneratorPolynomial {
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