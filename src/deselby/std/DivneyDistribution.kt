package deselby.std

import kotlin.math.absoluteValue
import kotlin.math.max

class DivneyDistribution private constructor(private val lambda : List<Double>, var coeffs : DoubleNDArray) {

    constructor(lambda : List<Double>) : this(
            DoubleArray(lambda.size,{lambda[it]}).asList(),
            DoubleNDArray(IntArray(lambda.size,{1}).asList(), {1.0})
    )

    // Transform P'(x_d) = P(x_d - 1) * x_d/lambda_d
    fun create(d : Int) : DivneyDistribution {
        coeffs.divAssign(lambda[d])
        shift(d,-1)
        multiplyByx(d)
        return this
    }

    // Transform P'(x_d) = lambda_d * P(x_d + 1)
    fun annihilate(d : Int) : DivneyDistribution {
        coeffs.timesAssign(lambda[d])
        shift(d,1)
        return this
    }


    operator fun plus(other : DivneyDistribution) : DivneyDistribution {
        if(coeffs.dimension.size != other.coeffs.dimension.size) {
            throw(IllegalArgumentException("Can't add distributions with different dimensions"))
        }
        if(lambda != other.lambda && !lambda.foldIndexed(true,{i,b,v -> b && v == other.lambda[i]})) {
            throw(IllegalArgumentException("Can't add distributions with different lambdas"))
        }
        val dimensionUnion = IntArray(coeffs.dimension.size,{ d -> max(coeffs.dimension[d], other.coeffs.dimension[d])})
        return DivneyDistribution(lambda, DoubleNDArray(dimensionUnion.asList()) { exponents ->
            coeffs[exponents] + other.coeffs[exponents]
        })
    }


    operator fun minus(other : DivneyDistribution) : DivneyDistribution {
        if(coeffs.dimension.size != other.coeffs.dimension.size) {
            throw(IllegalArgumentException("Can't subtract distributions with different dimensions"))
        }
        if(lambda != other.lambda && !lambda.foldIndexed(true,{i,b,v -> b && v == other.lambda[i]})) {
            throw(IllegalArgumentException("Can't subtract distributions with different lambdas"))
        }
        val dimensionUnion = IntArray(coeffs.dimension.size,{ d -> max(coeffs.dimension[d], other.coeffs.dimension[d])})
        return DivneyDistribution(lambda, DoubleNDArray(dimensionUnion.asList()) { exponents ->
            coeffs[exponents] - other.coeffs[exponents]
        })
    }

    operator fun times(const : Double) : DivneyDistribution {
        return DivneyDistribution(lambda, coeffs * const)
    }


    // shift in dim d by distance s (must be +-1)
    private fun shift(d : Int, s : Int) {
        for(ndIndex in coeffs.indexSet) {
            val n = ndIndex[d]
            var m = ndIndex[d]
            val cI = coeffs[ndIndex]
            var nChoosem = 1
            while(m > 0) {
                nChoosem *= s*m / (n - m + 1)
                m -= 1
                ndIndex[d] = m
                coeffs[ndIndex] += cI*nChoosem
            }
        }
    }

    // multiply by x_d
    private fun multiplyByx(d : Int) {
        val newDimension = coeffs.dimension.toMutableList()
        newDimension[d] += 1
        coeffs = DoubleNDArray(newDimension,{ exponents ->
            if(exponents[d] == 0) 0.0 else {
            exponents[d] -= 1
            coeffs[exponents]}
        })
    }

    override fun toString() : String {
        var s = "("
        val data = coeffs.asDoubleArray()
        var printPlus = false
        for(i in 0..data.lastIndex) {
            if(data[i] != 0.0) {
                if (printPlus) {
                    if(data[i] > 0) s += " + " else s += " - "
                } else {
                    if(data[i]<0) s += "-"
                    printPlus = true
                }
                val exponents = coeffs.toNDIndex(i)
                if (data[i] != 1.0 || i==0) s += data[i].absoluteValue.toString()
                for (j in 0..exponents.lastIndex) {
                    if (exponents[j] > 0) {
                        s += "${(j + 'a'.toByte()).toChar()}"
                        if (exponents[j] > 1) s += "^${exponents[j]}"
                    }
                }
            }
        }
        s += ")"
        for(i in 0..lambda.lastIndex) {
            val varName = (i+'a'.toByte()).toChar()
            s += "(${lambda[i]}^${varName}*e^${lambda[i]}/${varName}!)"
        }
        return s
    }

    fun copyOf() : DivneyDistribution {
        return DivneyDistribution(lambda, DoubleNDArray(coeffs.dimension, {coeffs[it]}))
    }

    fun numberOfCoeffsAbove(delta : Double) : Int {
        return coeffs.asDoubleArray().fold(0, {n, x -> if(x > delta) n+1 else  n})
    }

}