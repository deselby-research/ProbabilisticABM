package experiments

import deselby.distributions.DeselbyLambdaOptimiser
import org.apache.commons.math3.distribution.PoissonDistribution

fun main(args : Array<String>) {
    val p = PoissonDistribution(2.345)
    val coeffs = DoubleArray(11, {k ->
        p.probability(k)
    })
    println(coeffs.asList())
    val P = DeselbyLambdaOptimiser(coeffs)
    val Dlambda = P.opt()
    println(P.coeffs(Dlambda, 1e-7).asList())
    println(P.coeffs(2.345, 1e-7).asList())
}
