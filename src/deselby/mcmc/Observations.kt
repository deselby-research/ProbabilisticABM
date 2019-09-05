package deselby.mcmc

import org.apache.commons.math3.special.Gamma.logGamma
import org.apache.commons.math3.util.FastMath
import kotlin.math.exp
import kotlin.math.ln

class Observations(var logp : Double = 0.0) {
    fun gaussian(mu : Double, sigma : Double, observation : Double) : Observations {
        val x0 = observation - mu
        val x1 = x0 / sigma
        val logStandardDeviationPlusHalfLog2Pi = FastMath.log(sigma) + 0.5 * FastMath.log(6.283185307179586)
        logp -=  0.5 * x1 * x1 + logStandardDeviationPlusHalfLog2Pi
        return this
    }

    fun binomial(p : Double, n : Int, k : Int) : Observations {
        if(k > n || k < 0) {
            logp = Double.NEGATIVE_INFINITY
        } else {
            logp += k * ln(p) + (n - k) * ln(1 - p) + logGamma(n + 1.0) - logGamma(k + 1.0) - logGamma(n - k + 1.0)
        }
        return this
    }

//    fun getLogP() = logp

    fun getP() = exp(logp)
}