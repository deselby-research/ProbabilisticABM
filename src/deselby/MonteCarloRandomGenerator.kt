package deselby

import org.apache.commons.math3.special.Erf
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

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

    // perturb the random source so that calls to nextGaussian are updated
    // by a randSource.size dimensional Gaussian with no correlation and
    // SD of sigma (i.e. correlation matrix of sigma times the identity matrix)
    fun perturbWithGaussian(sigma : Double) : MonteCarloRandomGenerator {
            return MonteCarloRandomGenerator (randSource.size, { i ->
//                val perturbed = (Erf.erf(uniformToGaussian(randSource[i]) + rand.nextGaussian()*sigma) + 1.0)/2.0
                val perturbed = (randSource[i] + rand.nextGaussian()*sigma + 1.0).rem(1.0)
                perturbed
            })
    }

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

    inline fun uniformToGaussian(uniform : Double) : Double {
        return Erf.erfInv(uniform*2.0 - 1.0)
    }

}
