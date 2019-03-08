package deselby.distributions

import deselby.std.DoubleNDArray
import deselby.std.FallingFactorial
import kotlin.math.absoluteValue
import kotlin.math.min

class DeselbyDistribution private constructor(private val lambda : List<Double>, var coeffs : DoubleNDArray) {

    val dimension : List<Int>
        get() {
            return coeffs.dimension
        }

    constructor(lambda : List<Double>) : this(
            DoubleArray(lambda.size, {lambda[it]} ).asList(),
            DoubleNDArray(IntArray(lambda.size, { 1 }).asList(), { 1.0 })
    )


    fun create(d : Int) : DeselbyDistribution {
        val incrementedGeometry = dimension.mapIndexed { i, v ->
            if(i == d) v+1 else v
        }
        val shiftedCoeffs = DoubleNDArray(incrementedGeometry, { ndIndex ->
            if (ndIndex[d] == 0) 0.0 else {
                ndIndex[d] -= 1
                coeffs[ndIndex]
            }
        })
        return DeselbyDistribution(lambda, shiftedCoeffs)
    }


    // transform using identity
    // aP(lambda,D) = lambda*P(lambda,D) + DP(lambda,D-1)
    //
    fun annihilate(d : Int) : DeselbyDistribution {
        val newCoeffs = DoubleNDArray(dimension, { ndIndex ->
            lambda[d] * (coeffs[ndIndex]) + (++ndIndex[d]) * (coeffs.getOrNull(ndIndex) ?: 0.0)
        })
        return DeselbyDistribution(lambda, newCoeffs)
    }


    operator fun plus(other : DeselbyDistribution) : DeselbyDistribution {
        if(!isCompatible(other)) throw(IllegalArgumentException("Distributions are incompatible"))
        return DeselbyDistribution(lambda, coeffs + other.coeffs)
    }

    operator fun minus(other : DeselbyDistribution) : DeselbyDistribution {
        if(!isCompatible(other)) throw(IllegalArgumentException("Distributions are incompatible"))
        return DeselbyDistribution(lambda, coeffs - other.coeffs)
    }

    operator fun times(other : Double) : DeselbyDistribution {
        return DeselbyDistribution(lambda, coeffs * other)
    }

    // multiply this by a falling factorial
    operator fun times(factorial : FallingFactorial) : DeselbyDistribution {
        val newGeometry = dimension.mapIndexed { i, v ->
            if(i == factorial.variableId) v+factorial.order else v
        }
        val newCoeffs = DoubleNDArray(newGeometry, { 0.0 })
        for(ndIndex in coeffs.indexSet) {
            val delta = ndIndex[factorial.variableId]
            var ck = coeffs[ndIndex] // modified coefficient
            val writeIndex = ndIndex.copyOf()
            writeIndex[factorial.variableId] = factorial.order + delta
            for(k in 0..min(delta,factorial.order)) {
                newCoeffs[writeIndex] += ck
                --writeIndex[factorial.variableId]
                ck *= (factorial.order-k)*(delta-k)/(k+1.0)
            }
        }
        return DeselbyDistribution(lambda, newCoeffs)
    }


    fun isCompatible(other : DeselbyDistribution) : Boolean {
        if(dimension.size != other.dimension.size) return false
        if(lambda != other.lambda && !lambda.foldIndexed(true,{i,b,v -> b && v == other.lambda[i]})) return false
        return true
    }

    // return a copy of this with the smallest dimensions that
    // remove only terms that satisfy the given predicate
    fun shrinkTo(pred : (Double) -> Boolean) : DeselbyDistribution {
        val truncatedDimension = dimension.toIntArray()
        for(d in 0 until truncatedDimension.size) {
            while(truncatedDimension[d]>0 &&
                    coeffs.slice(d, truncatedDimension[d]-1).fold(true, {acc, v ->
                        acc && pred(v)
                    })) {
                --truncatedDimension[d]
            }
        }
        return DeselbyDistribution(lambda, DoubleNDArray(truncatedDimension.asList(), { coeffs[it] }))
    }

    fun truncateBelow(cutoff : Double) = shrinkTo({it.absoluteValue < cutoff})

    override fun toString() : String {
        var s = ""
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
                val ndIndex = coeffs.toNDIndex(i)
                if (data[i].absoluteValue != 1.0) s += data[i].absoluteValue.toString()
                s += "P${ndIndex.asList()}"
            }
        }
        return s
    }
}