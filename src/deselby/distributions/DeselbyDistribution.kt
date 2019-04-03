package deselby.distributions

import deselby.std.DoubleNDArray
import deselby.std.FallingFactorial
import koma.ndarray.NDArray
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.LUDecomposition
import java.lang.Math.pow
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.min

class DeselbyDistribution private constructor(val lambda : List<Double>, var coeffs : DoubleNDArray) : FockState<Int, DeselbyDistribution> {

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
        val shiftedCoeffs = DoubleNDArray(incrementedGeometry) { ndIndex ->
            if (ndIndex[d] == 0) 0.0 else {
                ndIndex[d] -= 1
                coeffs[ndIndex]
            }
        }
        return DeselbyDistribution(lambda, shiftedCoeffs)
    }


    // transform using identity
    // aP(lambda,D) = lambda*P(lambda,D) + DP(lambda,D-1)
    //
    override fun annihilate(d : Int) : DeselbyDistribution {
        val newCoeffs = DoubleNDArray(dimension) { ndIndex ->
            lambda[d] * (coeffs[ndIndex]) + (++ndIndex[d]) * (coeffs.getOrNull(ndIndex) ?: 0.0)
        }
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

    override operator fun times(const : Double) : DeselbyDistribution {
        return DeselbyDistribution(lambda, coeffs * const)
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
        val l = lambda[factorial.variableId]
        for(ndIndex in coeffs.indexSet) {
            val delta = ndIndex[factorial.variableId]
            var ck = coeffs[ndIndex]*pow(l,1.0*factorial.order) // this pow might get big!!!
            val writeIndex = ndIndex.copyOf()
            writeIndex[factorial.variableId] = factorial.order + delta
            for(q in 0..min(delta,factorial.order)) {
                newCoeffs[writeIndex] += ck
                --writeIndex[factorial.variableId]
                ck *= (factorial.order-q)*(delta-q)/((q+1.0)*l)
            }
        }
        return DeselbyDistribution(lambda, newCoeffs)
    }

    fun dotprod(other : DeselbyDistribution) : Double {
        return coeffs.dotprod(other.coeffs)
    }


    // observe that the variable with id 'variableId' has value 'm'
    // given that the probability of detection is 'p'
    // this amounts to multiplying this by the binomial distribution
    // P'(k) = Binom(p,m,k)P(k)
    // But Binom(p,m,k)P(l,k) = exp(-pl)(p/(1-p))^m/m! * (k)_m (1-p)^Delta P((1-p)l,k)
    fun binomialObserve(p : Double, m : Int, variableId : Int) : DeselbyDistribution {
        val lambdap = (1.0-p)*lambda[variableId]
        val newLambda = DoubleArray(lambda.size, {i -> if(i==variableId) lambdap else lambda[i]})
        var multiplier = exp(-p*lambda[variableId])
        val p1p = p/(1.0-p)
        for(i in 1..m) multiplier *= p1p/i
//        println("final multiplier = $multiplier")
        val premultipliedCoeffs = this.coeffs.mapIndexed { index, x ->
            x*multiplier*pow(1.0-p, 1.0*index[variableId])
        }
        val premultipliedDist = DeselbyDistribution(newLambda.asList(), premultipliedCoeffs)
//        println("premultiplied dist = $premultipliedDist")
        return premultipliedDist * FallingFactorial(variableId, m)
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

    fun integrateWithLambdaOptimisation(hamiltonian : (FockState<Int,DeselbyDistribution>)-> DeselbyDistribution, T : Double, dt : Double) : DeselbyDistribution {
        var p = this
        var time = 0.0
        while(time < T) {
            val dp = hamiltonian(p)*dt
            p = p.perturbWithLambda(dp).truncateBelow(1e-6)
//            p = p + dp
//            p = p.optimizeLambda().truncateBelow(1e-6)
            time += dt
        }
        return p
    }


    // returns this + perturbation, where the lambdas of the result are changed so as to
    // minimise the cartesian norm of the coefficients of the perturbation
    fun perturbWithLambda(perturbation : DeselbyDistribution) : DeselbyDistribution {
        val dP_dL = Array(dimension.size) {i ->
            this.annihilate(i).create(i)/this.lambda[i] - this
        }

        val Y = Array2DRowRealMatrix(Array(dimension.size) { i ->
            DoubleArray(1) {perturbation.dotprod(dP_dL[i])}
        })

        val M = Array2DRowRealMatrix(Array(dimension.size) { i ->
            DoubleArray(dimension.size) {j ->
                dP_dL[i].dotprod(dP_dL[j])
            }
        })

        val DL = LUDecomposition(M).solver.inverse.multiply(Y)

        val newLambda = DoubleArray(dimension.size) { i ->
            lambda[i] + DL.getEntry(i,0)
        }

        var newCoeffs = coeffs + perturbation.coeffs
        dP_dL.forEachIndexed { i, dP_dLi ->
            newCoeffs -= dP_dLi.coeffs * DL.getEntry(i, 0)
        }

        return DeselbyDistribution(newLambda.asList(), newCoeffs)
    }

    // returns this + perturbation, where the lambdas of the result are changed so as to
    // nullify the first order rates of change.
    fun perturbWithLambda2(perturbation : DeselbyDistribution) : DeselbyDistribution {
        val dP_dL = Array(dimension.size) { i ->
            this.create(i) - this
        }

        val zeroIndex = IntArray(dimension.size, {0})
        val DL = DoubleArray(dimension.size) { i ->
            val oneIndex = IntArray(dimension.size, {j -> if(j==i) 1 else 0})
            val dP1_dLi = coeffs[zeroIndex] + (1.0/lambda[i] - 1.0)*coeffs.getOrElse(oneIndex,{0.0})
            perturbation.coeffs[oneIndex] / dP1_dLi
        }

        val newLambda = DoubleArray(dimension.size) { i ->
            lambda[i] + DL[i]
        }
        var newPerturbation = perturbation.coeffs
        dP_dL.forEachIndexed { i, dP_dLi ->
            newPerturbation -= dP_dLi.coeffs * DL[i]
        }

        val newCoeffs = coeffs + newPerturbation

        return DeselbyDistribution(newLambda.asList(), newCoeffs)
    }

    // returns this + perturbation, where the lambdas of the result are changed so as to
    // minimise the weighted cartesian norm of the coefficients of the resulting polynomial
    //
    // assumes lambda is very close to optimisation initially
//    fun optimizeLambda() : DeselbyDistribution {
//        val P = this
//        val dP_dL = Array(P.dimension.size, {i ->
//            P.annihilate(i).create(i)/P.lambda[i] - P
//        })
//
//        val Y = Array2DRowRealMatrix(Array(dimension.size, { i ->
//            DoubleArray(1, {P.dotprod(dP_dL[i])})
//        }))
//
//        val M = Array2DRowRealMatrix(Array(dimension.size, { i ->
//            DoubleArray(dimension.size, {j ->
//                dP_dL[i].dotprod(dP_dL[j])})
//        }))
//
//        var DL = LUDecomposition(M).solver.inverse.multiply(Y)
//
//        if(DL.frobeniusNorm > 1e-3) {
//            DL = DL.scalarMultiply(1e-3/DL.frobeniusNorm)
//        }
//        println(DL.frobeniusNorm)
//
//        val newLambda = DoubleArray(dimension.size, {i ->
//            lambda[i] + DL.getEntry(i,0)
//        })
//        var newCoeffs = P.coeffs
//        dP_dL.forEachIndexed({ i, dP_dLi ->
//            newCoeffs -= dP_dLi.coeffs * DL.getEntry(i, 0)
//        })
//
//        return DeselbyDistribution(newLambda.asList(), newCoeffs)
//    }



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
                    coeffs.slice(d, truncatedDimension[d]-1).fold(true) { acc, v ->
                        acc && pred(v)
                    }) {
                --truncatedDimension[d]
            }
        }
        return DeselbyDistribution(lambda, DoubleNDArray(truncatedDimension.asList()) { coeffs[it] })
    }

    fun truncateBelow(cutoff : Double) = shrinkTo {it.absoluteValue < cutoff}

    fun renormalise() {
        val sumOfCoeffs = coeffs.asDoubleArray().sum()
        coeffs.timesAssign(1.0/sumOfCoeffs)
    }

    // Marginalise out all but the given dimension
    fun marginaliseTo(dim : Int) : DeselbyDistribution {
        val coeffs = DoubleNDArray(intArrayOf(dimension[dim])) {i ->
            coeffs.slice(dim, i[0]).fold(0.0, Double::plus)
        }
        return DeselbyDistribution(listOf(lambda[dim]), coeffs)
    }

    fun mean(dim : Int) : Double {
        val lambdai = lambda[dim]
        return coeffs.foldIndexed(0.0) {index, acc, x ->
            acc + x*(lambdai + index[dim])
        }
    }

    override fun toString() : String {
        var s = "L$lambda:"
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