package deselby.distributions

import deselby.std.LambdaList
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.reflect.jvm.internal.impl.protobuf.GeneratedMessageLite

class GeneratorPolynomial private constructor(val coeffs : HashMap<List<Int>,Double>) {
    val size : Int
    get() = coeffs.size

    constructor() : this(hashMapOf(listOf<Int>() to 1.0))

    fun create(d : Int) : GeneratorPolynomial = create(d,1)

    fun create(d : Int, n : Int) : GeneratorPolynomial {
        val result = HashMap<List<Int>,Double>()
        coeffs.forEach { occupationNo, prob ->
            val newOccupationNo = IntArray(max(occupationNo.size,d+1), {i ->
                if(i == d) (occupationNo.getOrNull(i)?:0) + n else occupationNo.getOrNull(i)?:0
            })
            result[newOccupationNo.asList()] = prob
        }
        return GeneratorPolynomial(result)
    }


    fun annihilate(d : Int) : GeneratorPolynomial {
        val result = HashMap<List<Int>,Double>()
        coeffs.forEach { occupationNo, prob ->
            if(occupationNo.size > d && occupationNo[d] > 0) {
                val nDim = if(occupationNo[d] == 1) occupationNo.size else max(occupationNo.size, d+1)
                val newOccupationNo = IntArray(nDim, { i ->
                    if (i == d) (occupationNo.getOrNull(i) ?: 0) - 1 else occupationNo.getOrNull(i) ?: 0
                })
                result[newOccupationNo.asList()] = prob*occupationNo[d]
            }
        }
        return GeneratorPolynomial(result)
    }

    operator fun plus(other : GeneratorPolynomial) : GeneratorPolynomial {
        val result = HashMap<List<Int>,Double>(coeffs)
        other.coeffs.forEach { otherIndex, otherVal ->
            result.merge(otherIndex, otherVal , Double::plus)
        }
        return GeneratorPolynomial(result)
    }

    operator fun minus(other : GeneratorPolynomial) : GeneratorPolynomial {
        val result = HashMap<List<Int>,Double>(coeffs)
        other.coeffs.forEach { otherIndex, otherVal ->
            result.merge(otherIndex, -otherVal , Double::plus)
        }
        return GeneratorPolynomial(result)
    }

    operator fun times(const : Double) : GeneratorPolynomial {
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