package deselby

import org.apache.commons.math3.util.FastMath

class Observations(var logp : Double = 0.0) {
    fun gaussian(mu : Double, sigma : Double, observation : Double) : Observations {
        val x0 = observation - mu
        val x1 = x0 / sigma
        val logStandardDeviationPlusHalfLog2Pi = FastMath.log(sigma) + 0.5 * FastMath.log(6.283185307179586)
        logp -=  0.5 * x1 * x1 + logStandardDeviationPlusHalfLog2Pi
        return this
    }

    fun getLogP() = logp
}