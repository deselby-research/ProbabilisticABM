package experiments.misc

import deselby.distributions.discrete.DeselbyLambdaOptimiser
import org.apache.commons.math3.distribution.PoissonDistribution
import org.junit.Test
import kotlin.math.min

class MiscExperiments {

    @Test
    fun lambdaOptimisation() {
        val p = PoissonDistribution(2.345)
        val coeffs = DoubleArray(11) { k ->
            p.probability(k)
        }
        println(coeffs.asList())
        val P = DeselbyLambdaOptimiser(coeffs)
        val Dlambda = P.opt()
        println(P.coeffs(Dlambda, 1e-7).asList())
        println(P.coeffs(2.345, 1e-7).asList())
    }

    @Test
    fun poissonDeath() {
        val N = 40
        var a = DoubleArray(N, {0.0})
        a[0] = 1.0
        val lambda = 8.0
        val dt = 0.0001

        for(t in 0..20000) {
            a = DoubleArray(N) {i ->
                when (i) {
                    0 -> a[0] + (lambda*a[0] + a[1])*dt
                    N-1 -> a[i] + ((lambda-i)*a[i] - lambda*a[i-1])*dt
                    else -> a[i] + ((lambda-i)*a[i] + (i+1)*a[i+1] - lambda*a[i-1])*dt
                }
            }
            println("${a.asList().subList(0, min(20,N))}")
        }

    }
}