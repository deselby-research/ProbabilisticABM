package deselby.mcmc

import deselby.std.extensions.nextGaussian
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.special.Erf
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.random.Random

class MonteCarloRandomGenerator(private val randSource : ArrayList<Double> = ArrayList(),
                                val rand : RandomGenerator = MersenneTwister()) : RandomGenerator {

    private var sourceCounter = 0

    fun reset() {
        sourceCounter = 0
    }

    constructor(size : Int, randFunc : (Int) -> Double) : this(ArrayList(size)) {
        for(i in 0 until size) {
            randSource.add(randFunc(i))
        }
    }

    fun perturbWithGaussian(sigma : Double) = gaussianProposal(this, sigma)

    override fun nextDouble(): Double {
        if(sourceCounter == randSource.size) randSource.add(rand.nextDouble())
        return randSource[sourceCounter++]
    }

    override fun nextGaussian() = uniformToGaussian(nextDouble())
    override fun nextBoolean() = (nextInt(2) == 0)
    override fun nextFloat() = nextDouble().toFloat()
    override fun nextInt(i: Int) = (nextDouble() * i).toInt()
    override fun nextInt() =
            (1.0 * Integer.MIN_VALUE + nextDouble() * (1.0 * Integer.MAX_VALUE - 1.0 * Integer.MIN_VALUE + 1.0)).toInt()
    override fun nextLong()=
            (1.0 * Long.MIN_VALUE + nextDouble() * (1.0 * Long.MAX_VALUE - 1.0 * Long.MIN_VALUE + 1.0)).toLong()

    override fun setSeed(i: Int) {
        rand.setSeed(i)
    }

    override fun setSeed(ints: IntArray) {
        rand.setSeed(ints)
    }

    override fun setSeed(l: Long) {
        rand.setSeed(l)
    }

    override fun nextBytes(bytes: ByteArray) {
        var i = 0
        var j: Int
        var r: Int
        while (i < bytes.size) {
            r = nextInt()
            j = 0
            while (j < 32 && i < bytes.size) {
                bytes[i++] = (r shr j and 0xff).toByte()
                j += 8
            }
        }
    }


    companion object {
        // perturb the random source so that calls to nextGaussian are updated
        // by a randSource.nDimensions dimensional Gaussian with no correlation and
        // SD of sigma (i.e. correlation matrix of sigma create the identity matrix)
        fun gaussianProposal(currentState: MonteCarloRandomGenerator, sigma : Double = 0.1) : MonteCarloRandomGenerator {
            return MonteCarloRandomGenerator(currentState.randSource.size) { i ->
                val y = currentState.randSource[i] + Random.nextGaussian() * sigma
                y - floor(y)
            }
        }


        fun singlePerturbationGaussianProposal(currentState: MonteCarloRandomGenerator, sigma : Double = 0.1) : MonteCarloRandomGenerator {
            val perturbed = MonteCarloRandomGenerator(ArrayList(currentState.randSource))
            if(currentState.sourceCounter > 0) {
                val indexToPerturb = Random.nextInt(currentState.sourceCounter)
                val perturbedVal = perturbed.randSource[indexToPerturb] + Random.nextGaussian() * sigma
                perturbed.randSource[indexToPerturb] = perturbedVal - floor(perturbedVal)
            }
            return perturbed
        }



        inline fun uniformToGaussian(uniform : Double) : Double {
            return sqrt(2.0) * Erf.erfInv(uniform*2.0 - 1.0)
        }
    }

}
