package deselby.distributions

import deselby.std.DoubleNDArray
import deselby.std.FallingFactorial
import kotlin.math.absoluteValue
import kotlin.math.min

class DeselbyDistribution private constructor(private val lambda : List<Double>, var coeffs : DoubleNDArray) : FockState<Int, DeselbyDistribution> {

    val dimension : List<Int>
        get() {
            return coeffs.dimension
        }

    constructor(lambda : List<Double>) : this(
            DoubleArray(lambda.size, {lambda[it]} ).asList(),
            DoubleNDArray(IntArray(lambda.size, { 1 }).asList(), { 1.0 })
    )


    override fun create(d : Int) : DeselbyDistribution {
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
    override fun annihilate(d : Int) : DeselbyDistribution {
        val newCoeffs = DoubleNDArray(dimension, { ndIndex ->
            lambda[d] * (coeffs[ndIndex]) + (++ndIndex[d]) * (coeffs.getOrNull(ndIndex) ?: 0.0)
        })
        return DeselbyDistribution(lambda, newCoeffs)
    }


    override operator fun plus(other : DeselbyDistribution) : DeselbyDistribution {
        if(!isCompatible(other)) throw(IllegalArgumentException("Distributions are incompatible"))
        return DeselbyDistribution(lambda, coeffs + other.coeffs)
    }

    override operator fun minus(other : DeselbyDistribution) : DeselbyDistribution {
        if(!isCompatible(other)) throw(IllegalArgumentException("Distributions are incompatible"))
        return DeselbyDistribution(lambda, coeffs - other.coeffs)
    }

    override operator fun times(other : Double) : DeselbyDistribution {
        return DeselbyDistribution(lambda, coeffs * other)
    }

    operator fun div(other : Double) : DeselbyDistribution {
        return DeselbyDistribution(lambda, coeffs / other)
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


    // observe that the variable with id 'variableId' has value 'm'
    // given that the probability of detection is 'p'
    // this amounts to multiplying this by the binomial distribution
    // P'(k) = Binom(p,m,k)P(k)
    // But Binom(p,m,k)P(l,k) = exp(-pl)(p/(1-p))^m/m! * (k)_m P((1-p)l,k)
    fun binomialObserve(p : Double, m : Int, variableId : Int) : DeselbyDistribution {
        var multiplier = 1.0
        val p1p = p/(1.0-p)
        for(i in 1..m) multiplier *= p1p/i
        val result = this*FallingFactorial(variableId, m)*multiplier
        val newLambda = DoubleArray(lambda.size, {i -> if(i==variableId) (1.0-p)*lambda[i] else lambda[i]})
        return DeselbyDistribution(newLambda.asList(), result.coeffs)
    }


    fun integrate(hamiltonian : (FockState<Int,DeselbyDistribution>)-> DeselbyDistribution, T : Double, dt : Double) : DeselbyDistribution {
        var p = this
        var time = 0.0
        while(time < T) {
            p = (p + hamiltonian(p)*dt).truncateBelow(1e-8)
            time += dt
        }
        return p
    }


    // returns this + perturbation, where the lambdas of the result are changed so as to
    // minimise the cartesian norm of the coefficients of the perturbation
    fun perturbWithLambda(perturbation : DeselbyDistribution) : DeselbyDistribution {
        val dP_dL = Array(dimension.size, {i ->
            this.annihilate(i).create(i)/this.lambda[i] - this
        })
        return this
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