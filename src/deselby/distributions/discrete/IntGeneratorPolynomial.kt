package deselby.distributions.discrete

import deselby.distributions.FockState
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.ln
import kotlin.math.max

// For representing agents whose state maps to the set of integers.
// This is a dense representation, so only suitable when there are
// a small number of states. Use GeneratorPolynomial for a sparse
// representation
class IntGeneratorPolynomial private constructor(val coeffs : HashMap<List<Int>,Double>) : FockState<Int, IntGeneratorPolynomial> {
    val size : Int
    get() = coeffs.size

    constructor() : this(hashMapOf(listOf<Int>() to 1.0))

    override fun create(d : Int) : IntGeneratorPolynomial = create(d,1)

    fun create(d : Int, n : Int) : IntGeneratorPolynomial {
        val result = HashMap<List<Int>,Double>()
        coeffs.forEach { occupationNo, prob ->
            val newOccupationNo = IntArray(max(occupationNo.size,d+1)) {i ->
                if(i == d) (occupationNo.getOrNull(i)?:0) + n else occupationNo.getOrNull(i)?:0
            }
            result[newOccupationNo.asList()] = prob
        }
        return IntGeneratorPolynomial(result)
    }


    override fun annihilate(d : Int) : IntGeneratorPolynomial {
        val result = HashMap<List<Int>,Double>()
        coeffs.forEach { occupationNo, prob ->
            if(occupationNo.size > d && occupationNo[d] > 0) {
                val nDim = if(occupationNo[d] == 1) occupationNo.size else max(occupationNo.size, d+1)
                val newOccupationNo = IntArray(nDim) { i ->
                    if (i == d) (occupationNo.getOrNull(i) ?: 0) - 1 else occupationNo.getOrNull(i) ?: 0
                }
                result[newOccupationNo.asList()] = prob*occupationNo[d]
            }
        }
        return IntGeneratorPolynomial(result)
    }

    override operator fun plus(other : IntGeneratorPolynomial) : IntGeneratorPolynomial {
        val result = HashMap<List<Int>,Double>(coeffs)
        other.coeffs.forEach { otherIndex, otherVal ->
            result.merge(otherIndex, otherVal , Double::plus)
        }
        return IntGeneratorPolynomial(result)
    }

    override operator fun minus(other : IntGeneratorPolynomial) : IntGeneratorPolynomial {
        val result = HashMap<List<Int>,Double>(coeffs)
        other.coeffs.forEach { otherIndex, otherVal ->
            result.merge(otherIndex, -otherVal , Double::plus)
        }
        return IntGeneratorPolynomial(result)
    }

    override operator fun times(const : Double) : IntGeneratorPolynomial {
        val result = HashMap<List<Int>,Double>()
        coeffs.forEach { occupationNo, prob ->
            result[occupationNo] = prob*const
        }
        return IntGeneratorPolynomial(result)
    }

    operator fun get(index : List<Int>) = coeffs[index]

    operator fun set(index : List<Int>, v : Double) {
        coeffs[index] = v
    }

    // randomly chooses a single monomial from this polynomial with probability proportional to
    // its coefficient and returns a IntGeneratorPolynomial with just that monomial in, with
    // coefficient of 1
    // assumes that this polynomial is normalised
    fun sample(rand : RandomGenerator = MersenneTwister()) : IntGeneratorPolynomial {
        val targetCumulativeProb = rand.nextDouble()
        var cumulativeProb = 0.0
        val iterator = coeffs.iterator()
        var entry : MutableMap.MutableEntry<List<Int>,Double>? = null
        while(cumulativeProb <= targetCumulativeProb && iterator.hasNext()) {
            entry = iterator.next()
            cumulativeProb += entry.value
        }
        val result = IntGeneratorPolynomial(HashMap())
        if(entry != null) result[entry.key] = 1.0
        return result
    }

    // returns a (dt, monomial) pair where dt is the time elapsed, and monomial is the state that is transitioned
    // to from this.
    fun sampleNext(hamiltonian : (FockState<Int, IntGeneratorPolynomial>) -> IntGeneratorPolynomial, rand : RandomGenerator = MersenneTwister()) : Pair<Double, IntGeneratorPolynomial> {
        val p0 = if(this.size == 1) this else this.sample()
        val H = hamiltonian(p0)
        if(H.size == 0) return Pair(Double.POSITIVE_INFINITY, this)
        val currentState = p0.coeffs.keys.first()
        val totalRate = -(H[currentState]?:throw(IllegalArgumentException()))
        H.coeffs.remove(currentState)
        return Pair(
                -ln(1.0 - rand.nextDouble())/totalRate,
                (H*(1.0/totalRate)).sample(rand)
        )
    }

    // the sum of all coefficients
    fun norm1() : Double {
        return coeffs.values.sum()
    }


    override fun toString() : String {
        var s = ""
        var printPlus = false
        coeffs.forEach { occupation, p ->
            if (printPlus) {
                if(p > 0.0) s += " + " else s += " - "
            } else {
                if(p < 0.0) s += "-"
                printPlus = true
            }
            if (p.absoluteValue != 1.0) s += p.absoluteValue.toString()
            s += "P${occupation}"
        }
        return s
    }
}
