package deselby.distributions

import org.apache.commons.math3.optim.InitialGuess
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.SimpleValueChecker
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min

class DeselbyLambdaOptimiser(val C : DoubleArray) {
    fun opt() : Double {
        val answer = NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                SimpleValueChecker(1e-8, 1e-8)
        ).optimize(
                ObjectiveFunction({ objective(it[0]) }),
                ObjectiveFunctionGradient({ doubleArrayOf(grad(it[0])) }),
                GoalType.MINIMIZE,
                InitialGuess(doubleArrayOf(0.0)),
                MaxEval(1000)
        )

        println(answer.point.asList())
        return answer.point[0]
    }


    fun objective(Dlambda: Double): Double {
        var ci  : Double
        var si  : Double
        val eDlambda = exp(Dlambda)
        var i = 0
        var total = 0.0
        do {
            ci = csum(i,Dlambda)
            si = weight(i)*ci*ci
            total += si
            i += 1
        } while(i<C.size || si > 1e-16)
        return total*eDlambda*eDlambda
    }


    fun grad(Dlambda: Double): Double {
        var dO_dl = C[0] * weight(0) * C[0]
        for(i in 1 until C.size) {
            dO_dl += C[i] * weight(i) * (C[i] - C[i-1])
        }
        return 2.0 * dO_dl
    }

    fun weight(delta : Int) : Double {
//        return exp(1.0*delta) - 1.0
//        return if(delta < C.size-1) 0.0 else 1.0
        return 1.0/(1.0 + exp(-1.5*(delta - C.size + 1.0)))
    }

    fun coeffs(Dlambda : Double, truncationCutoff : Double) : DoubleArray {
        val newC = ArrayList<Double>(C.size)
        val eDlambda = exp(Dlambda)
        var i = 0
        var ci = eDlambda * csum(i, Dlambda)
        do {
            newC.add(ci)
            ci = eDlambda * csum(++i, Dlambda)
        } while(i<C.size || abs(ci)>truncationCutoff)
        var lasti = newC.lastIndex
        while(lasti>=0 && newC[lasti] < truncationCutoff) {--lasti}
        return newC.subList(0, lasti+1).toDoubleArray()
    }

    inline fun csum(i : Int, Dlambda : Double) : Double {
        var f = 1.0
        var ci = C.getOrNull(i)?:0.0
        for(j in 1..i) {
            f *= -Dlambda/j
            ci += f*(C.getOrNull(i-j)?:0.0)
        }
        return ci
    }
}