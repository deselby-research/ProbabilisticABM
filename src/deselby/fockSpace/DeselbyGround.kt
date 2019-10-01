package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector
import org.apache.commons.math3.distribution.PoissonDistribution
import java.io.Serializable
import kotlin.math.pow

class DeselbyGround<AGENT>(val lambdas : Map<AGENT,Double>) : Ground<AGENT>, Serializable {

    override fun preMultiply(basis: Basis<AGENT>, termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
        var multiplier = 1.0
        basis.annihilations.forEach { (d, n) ->
            multiplier *= (lambdas[d]?:0.0).pow(n)
        }
        if(multiplier != 0.0) termConsumer(CreationBasis(basis.creations), multiplier)
    }

    override fun annihilate(d: AGENT): DoubleVector<CreationBasis<AGENT>> {
        return OneHotDoubleVector(Basis.identity(),lambdas[d]?:0.0)
    }

    override fun lambda(d : AGENT) = this.lambdas[d]?:0.0

//    fun mean(state: CreationVector<AGENT>) : Map<AGENT,Double> {
//        var mean = HashMap<AGENT,Double>()
//        lambdas.mapValuesTo(mean) {0.0}
//        state.forEach { stateTerm ->
//            // TODO: inefficient: most bases in each stateTerm will have zero exponent
//            mean.entries.forEach { meanEntry ->
//                val dmean = stateTerm.key[meanEntry.key] + lambda(meanEntry.key)
//                val newVal = meanEntry.value + dmean*stateTerm.value
//                meanEntry.setValue(newVal)
//            }
//        }
//        return mean
//    }

    override fun toString() : String {
        return lambdas.toString()
    }

    companion object {
        fun logProbability(k: Int, delta: Int, lambda: Double) =
                PoissonDistribution(null, lambda, 0.1, 1).logProbability(k - delta)

        fun probability(k: Int, delta: Int, lambda: Double) =
                PoissonDistribution(null, lambda, 0.1, 1).probability(k - delta)
    }
}