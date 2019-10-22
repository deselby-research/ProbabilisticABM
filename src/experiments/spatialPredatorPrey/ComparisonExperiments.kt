package experiments.spatialPredatorPrey

import deselby.fockSpace.BinomialBasis
import deselby.fockSpace.CreationBasis
import experiments.reverseSummation.PredPreyExperiments
import models.predatorPrey.Params
import models.predatorPrey.discreteEventModel.Agent
import models.predatorPrey.discreteEventModel.Prey
import models.predatorPrey.toFockMap
import org.junit.Test

class ComparisonExperiments {
    val deExperiments = DiscreteEventModelExperiments()
    val fockExperiments = PredPreyExperiments()

    @Test
    fun testPriors() {
        val params = Params(
                8,
                0.05,
                0.05,
                0.03,
                0.06,
                1.0,
                0.07,
                0.05,
                0.5,
                1.0
        )
        val startState = mapOf<Agent,Int>(Prey(0, 0, params.GRIDSIZE) to 1)
        val startBasis = CreationBasis(startState.toFockMap())
        val time = 0.5
//        val deMeans = deExperiments.prior(startState, params, time, 1000000)
//        println(deMeans.entries.sortedByDescending { it.value })
//        val fockMeans = fockExperiments.monteCarloPrior(startBasis, params, time, 1000000)
//        println(fockMeans.entries.sortedByDescending { it.value })
        val reverseMeans = fockExperiments.reverseIntegralPrior(startBasis, params, time)
        println(reverseMeans.entries.sortedByDescending { it.value })
//        fockExperiments.plot(fockMeans, params.GRIDSIZE)
    }

    @Test
    fun testPosteriors() {
        val params = Params(
                4,
                0.01,
                0.01,
                0.03,
                0.06,
                1.0,
                0.07,
                0.05,
                0.5,
                1.0
        )
        val startState = mapOf<Agent,Int>(Prey(0, 0, params.GRIDSIZE) to 1)
        val observation = mapOf<Agent,Int>(Prey(1, 0, params.GRIDSIZE) to 1)
        val intObservation = observation.mapKeys { it.key.hashCode() }
        val startBasis = CreationBasis(startState.toFockMap())
        val time = 0.5
        val pObserve = 0.5
        val observationBasis = BinomialBasis(pObserve, observation.toFockMap())
        val deMeans = deExperiments.posterior(startState, params, intObservation, pObserve, time, 1000000)
//        println(deMeans)
        println(deMeans.entries.sortedByDescending { it.value })
        val fockMeans = fockExperiments.posterior(startBasis, params, observationBasis, time, 1000000)
//        println(fockMeans)
        println(fockMeans.entries.sortedByDescending { it.value })
     //   fockExperiments.plot(fockMeans, params.GRIDSIZE)
    }

}