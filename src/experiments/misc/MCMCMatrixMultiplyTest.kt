package experiments.misc

import deselby.mcmc.MetropolisHastings
import deselby.mcmc.MonteCarloRandomGenerator
import deselby.std.extensions.*
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.random.RandomGenerator
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sign
import kotlin.random.Random

class MCMCMatrixMultiplyTest {

    // Attempt to calculate matrix to the power nAnnihilations using
    // phased mcmc.
    // Works to an extent but unstable for large nAnnihilations
    @Test
    fun matrixMultiply() {
        val nPow = 16
        val size = 3
        val I = MatrixUtils.createRealIdentityMatrix(size)
//        val M = I + (Random.nextDoubleMatrix(size,size) - 0.5)*0.01
        var M : RealMatrix = randomTransitionHamiltonian(size) //*0.01 + I
        M = M - I*(M.trace/size)
        val Xn = Random.nextDoubleMatrix(size,1)
        val X = Xn/Xn.norm
//        val X = Array2DRowRealMatrix(arrayOf(doubleArrayOf(0.5, 0.4, 0.1))).transpose()
        val product = M.power(nPow)*X
        val exactP = product / product.norm
        println("M = $M")
        println("X = $X")
        println("M^${nPow}X = $product")
        println("renormalised = ${exactP}")

//        val mcmc = MetropolisHastings({MonteCarloRandomGenerator.gaussianProposal(it,0.5)}) {
//            generateSample(M,nPow,X,it)
//        }
        val mcmc = MetropolisHastings({MonteCarloRandomGenerator.singlePerturbationGaussianProposal(it,0.5)}) {
            generateSample(M,nPow,X,it)
        }
        val nSamples = 1000000
        val samples = mcmc.sampleToList(nSamples).drop(1000)
        val P = Array2DRowRealMatrix( Array(1) {DoubleArray(size) {i ->
            samples.asSequence().map { if(abs(it)-1 == i) it.sign else 0 }.sum().toDouble()
        }}).transpose()
        val renormP = P / P.norm
        println("Sampled mean = $renormP")
        println("Error = ${renormP - exactP}")
    }


    fun generateSample(M: RealMatrix, toPowerN: Int, X: RealMatrix, rand: RandomGenerator): Pair<Double,Int> {
        val size = X.rowDimension
        if(M.rowDimension != size || M.columnDimension != size) throw(IllegalArgumentException("Matrix is wrong size"))
        var row = rand.nextInt(size)
//        var weight = X[row, 0]
        var phase = X[row,0].sign
        var logWeight = ln(abs(X[row,0]))
        for(i in 1..toPowerN) {
            val nextRow = rand.nextInt(size)
//            weight *= M[nextRow, row]
            val w = M[nextRow,row]
            phase *= w.sign
            logWeight += ln(abs(w))
            row = nextRow
        }
        return Pair(logWeight, (row+1)*phase.toInt())
    }

    fun randomTransitionHamiltonian(size: Int): Array2DRowRealMatrix {
        val M = Array2DRowRealMatrix(size, size)
        for(j in 0 until size) {
            M[j,j] = 0.0
            for(i in 0 until size) {
                if(i != j) {
                    M[i, j] = Random.nextDouble()
                    M[j,j] -= M[i,j]
                }
            }
        }
        return M
    }
}