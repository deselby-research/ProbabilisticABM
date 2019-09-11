package deselby.std.extensions

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.special.Erf
import org.apache.commons.math3.special.Gamma
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

object Distributions {

    // Probability of anything less than k
    fun PoissonCdf(k : Int, lambda : Double) : Double {
        if(k <= 0) return 0.0
        return Gamma.regularizedGammaQ(k.toDouble(), lambda)
    }


    // Samples from a Poisson distribution. This uses only one call to the
    // random number generator, unlike many other algorithms. This is
    // desirable when doing MCMC.
    // Algorithm adapted from Numerical Recipies, Third Edition and
    // Wikipedia "Poisson distribution"
    fun nextPoisson(nextDouble: Double, lambda : Double) : Int {
        var k = 0

        if(lambda < 100) {
            var P = exp(-lambda)
            var cumulativeP = P
            while(cumulativeP < nextDouble) {
                ++k
                P *= lambda/k
                cumulativeP += P
            }
            return k
        } else {
            var upperK : Int
            var lowerK : Int
            var inc = 1
            if(nextDouble < exp(-lambda)) return 0
            k = (sqrt(2.0*lambda)*Erf.erfInv(nextDouble*2.0 - 1.0) + lambda + 0.75).toInt() // Start with Gaussian approximation rounded up
            if(nextDouble < PoissonCdf(k, lambda)) {
                do {
                    k = max(k-inc,0)
                    inc *= 2
                } while(nextDouble < PoissonCdf(k, lambda))
                lowerK = k
                upperK = k + inc/2
            } else {
                do {
                    k += inc
                    inc *= 2
                } while(nextDouble > PoissonCdf(k, lambda))
                lowerK = k - inc/2
                upperK = k
            }
            while(upperK - lowerK > 1) {
                k = (lowerK + upperK)/2
                if(nextDouble < PoissonCdf(k, lambda))
                    upperK = k
                else
                    lowerK = k
            }
            return lowerK
        }
    }

    // Exponential distribution
    // P(x) = lambda*exp(-lambda*x)
    // for x > 0
    inline fun nextExponential(nextDouble: Double, lambda : Double) : Double {
        return -ln(1.0-nextDouble)/lambda
    }

    fun nextCategorical(nextDouble: Double, p0: Double, vararg pn: Double) : Int {
        val cdf = ArrayList<Double>(pn.size+1)
        cdf.add(p0)
        var t = p0
        for(i in pn.indices) {
            t += pn[i]
            cdf.add(t)
        }
        val insertionPoint = cdf.binarySearch(nextDouble*t)
        if(insertionPoint < 0) return -(insertionPoint + 1)
        return insertionPoint
    }
}


inline fun RandomGenerator.nextPoisson(lambda : Double) : Int {
    return Distributions.nextPoisson(nextDouble(), lambda)
}

inline fun RandomGenerator.nextExponential(lambda : Double) : Double {
    return Distributions.nextExponential(nextDouble(), lambda)
}

inline fun RandomGenerator.nextCategorical(p0 : Double, vararg pn: Double) : Int {
    return Distributions.nextCategorical(nextDouble(), p0, *pn)
}

inline fun Random.nextPoisson(lambda : Double) : Int {
    return Distributions.nextPoisson(nextDouble(), lambda)
}

inline fun Random.nextExponential(lambda : Double) : Double {
    return Distributions.nextExponential(nextDouble(), lambda)
}

inline fun Random.nextCategorical(p0 : Double, vararg pn: Double) : Int {
    return Distributions.nextCategorical(nextDouble(), p0, *pn)
}

inline fun Random.nextGaussian(): Double {
    return sqrt(2.0) * Erf.erfInv(nextDouble()*2.0 - 1.0)
}

// Creates a matrix of given size filled with 'nextDouble's
fun Random.nextDoubleMatrix(iSize: Int, jSize: Int): Array2DRowRealMatrix {
    return Array2DRowRealMatrix(Array(iSize) {
        DoubleArray(jSize) {
            this.nextDouble()
        }
    })
}