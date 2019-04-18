package deselby.distributions.discrete

import deselby.distributions.FockState
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.ln
import kotlin.math.max

class GeneratorPolynomial private constructor(val coeffs : HashMap<List<Int>,Double>) : FockState<Int, GeneratorPolynomial> {
    val size : Int
    get() = coeffs.size

    constructor() : this(hashMapOf(listOf<Int>() to 1.0))

    override fun create(d : Int) : GeneratorPolynomial = create(d,1)

    fun create(d : Int, n : Int) : GeneratorPolynomial {
        val result = HashMap<List<Int>,Double>()
        coeffs.forEach { occupationNo, prob ->
            val newOccupationNo = IntArray(max(occupationNo.size,d+1)) {i ->
                if(i == d) (occupationNo.getOrNull(i)?:0) + n else occupationNo.getOrNull(i)?:0
            }
            result[newOccupationNo.asList()] = prob
        }
        return GeneratorPolynomial(result)
    }


    override fun annihilate(d : Int) : GeneratorPolynomial {
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
        return GeneratorPolynomial(result)
    }

    override operator fun plus(other : GeneratorPolynomial) : GeneratorPolynomial {
        val result = HashMap<List<Int>,Double>(coeffs)
        other.coeffs.forEach { otherIndex, otherVal ->
            result.merge(otherIndex, otherVal , Double::plus)
        }
        return GeneratorPolynomial(result)
    }

    override operator fun minus(other : GeneratorPolynomial) : GeneratorPolynomial {
        val result = HashMap<List<Int>,Double>(coeffs)
        other.coeffs.forEach { otherIndex, otherVal ->
            result.merge(otherIndex, -otherVal , Double::plus)
        }
        return GeneratorPolynomial(result)
    }

    override operator fun times(const : Double) : GeneratorPolynomial {
        val result = HashMap<List<Int>,Double>()
        coeffs.forEach { occupationNo, prob ->
            result[occupationNo] = prob*const
        }
        return GeneratorPolynomial(result)
    }

    operator fun get(index : List<Int>) = coeffs[index]

    operator fun set(index : List<Int>, v : Double) {
        coeffs[index] = v
    }

    // randomly chooses a single monomial from this polynomial with probability proportional to
    // its coefficient and returns a GeneratorPolynomial with just that monomial in, with
    // coefficient of 1
    // assumes that this polynomial is normalised
    fun sample(rand : RandomGenerator = MersenneTwister()) : GeneratorPolynomial {
        val targetCumulativeProb = rand.nextDouble()
        var cumulativeProb = 0.0
        val iterator = coeffs.iterator()
        var entry : MutableMap.MutableEntry<List<Int>,Double>? = null
        while(cumulativeProb <= targetCumulativeProb && iterator.hasNext()) {
            entry = iterator.next()
            cumulativeProb += entry.value
        }
        val result = GeneratorPolynomial(HashMap())
        if(entry != null) result[entry.key] = 1.0
        return result
    }

    // returns a (dt, monomial) pair where dt is the time elapsed, and monomial is the state that is transitioned
    // to from this.
    fun sampleNext(hamiltonian : (FockState<Int, GeneratorPolynomial>) -> GeneratorPolynomial, rand : RandomGenerator = MersenneTwister()) : Pair<Double, GeneratorPolynomial> {
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
