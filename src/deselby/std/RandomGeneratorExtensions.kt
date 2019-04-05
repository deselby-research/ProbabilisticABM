package deselby.std

import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.special.Erf
import org.apache.commons.math3.special.Erf.erf
import org.apache.commons.math3.special.Gamma
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt

private object PoissonDistribution {
    // Probability of anything less than k
    fun cdf(k : Int, lambda : Double) : Double {
        if(k <= 0) return 0.0
        return Gamma.regularizedGammaQ(k.toDouble(), lambda)
    }
}


// Samples from a Poisson distribution. This uses only one call to the
// random number generator, unlike many other algorithms. This is
// desirable when doing MCMC.
// Algorithm adapted from Numerical Recipies, Third Edition and
// Wikipedia "Poisson distribution"
fun RandomGenerator.nextPoisson(lambda : Double) : Int {
    val target = nextDouble()
    var k = 0

    if(lambda < 100) {
        var P = exp(-lambda)
        var cumulativeP = P
        while(cumulativeP < target) {
            ++k
            P *= lambda/k
            cumulativeP += P
        }
        return k
    } else {
        var upperK : Int
        var lowerK : Int
        var inc = 1
        if(target < exp(-lambda)) return 0
        k = (sqrt(2.0*lambda)*Erf.erfInv(target*2.0 - 1.0) + lambda + 0.75).toInt() // Start with Gaussian approximation rounded up
        if(target < PoissonDistribution.cdf(k, lambda)) {
            do {
                k = max(k-inc,0)
                inc *= 2
            } while(target < PoissonDistribution.cdf(k, lambda))
            lowerK = k
            upperK = k + inc/2
        } else {
            do {
                k += inc
                inc *= 2
            } while(target > PoissonDistribution.cdf(k, lambda))
            lowerK = k - inc/2
            upperK = k
        }
        while(upperK - lowerK > 1) {
            k = (lowerK + upperK)/2
            if(target < PoissonDistribution.cdf(k, lambda))
                upperK = k
            else
                lowerK = k
        }
        return lowerK
    }
}
