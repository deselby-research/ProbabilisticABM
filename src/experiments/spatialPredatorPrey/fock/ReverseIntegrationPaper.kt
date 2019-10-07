package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.Basis
import deselby.fockSpace.BinomialBasis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.extensions.logProb
import experiments.spatialPredatorPrey.Params
import experiments.spatialPredatorPrey.TenByTenParams
import experiments.spatialPredatorPrey.TestParams
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.math.ln
import kotlin.random.Random
import kotlin.system.measureTimeMillis

object ReverseIntegrationPaperParams : Params(
        32,
        0.03,
        0.06,
        0.1,
        0.15,
        1.0,
        0.1,
        0.0,
        0.5,
        1.0
)


fun main(args: Array<String>) {
//    readLog("data/experiment2/Experiment1570017336847.dat") // best
//    readLog("data/experiment2/Experiment1570037250091.dat") //worst
//    readLog("data/experiment2/Experiment1570079875376.dat") //midway

    for(i in 1..5) {
        assimilationRun()
    }
}


fun assimilationRun() {
    val experimentName = "Experiment${Date().time}"
    val rawOut = ObjectOutputStream(FileOutputStream("${experimentName}.dat"))
    val analysisOut = OutputStreamWriter(FileOutputStream("${experimentName}.csv"))

    val sim = Simulation(ReverseIntegrationPaperParams)
    val priorGround = sim.D0.lambdas

    val obsInterval = 0.5
    val nWindows = 64   // number of assimilation windows
    val pLook = 0.02     // probability of looking at an agent state
    val pObserve = 0.9  // probability of detecting an agent given that we're looking
    var state = Basis.identity<Agent>()
    val allObservations = ObservationGenerator.generate(sim.params, pObserve, nWindows, obsInterval)
    val observations = allObservations.map { observation ->
        Pair(observation.real, BinomialBasis(observation.observed.pObserve,
                sim.D0.lambdas.keys
                        .filter { Random.nextDouble() < pLook }
                        .associateWith { observation.observed.observations[it]?:0 }
        ))
    }

    rawOut.writeObject(sim.params)

    var i = 0

    observations.forEach { (realState, binomObs) ->
        ++i
        val (nextState, nextD0) = sim.reversePosterior(state, binomObs, obsInterval, 3)
        sim.D0 = nextD0
        state = nextState
        val binomLogProb = binomObs.logProb(realState, priorGround)
        val posteriorLogProb = state.asGroundedBasis(sim.D0).logProb(realState)

        rawOut.writeObject(realState)
        rawOut.writeObject(binomObs)
        rawOut.writeObject(state)
        rawOut.writeObject(sim.D0)
        rawOut.flush()

        analysisOut.write("$i $binomLogProb $posteriorLogProb ${posteriorLogProb - binomLogProb}\n")
        analysisOut.flush()
    }
    rawOut.close()
    analysisOut.close()
}

fun readLog(filename: String) {
    val log = AssimilationLog(filename)
    println("read log of length ${log.records.size}")
//    log.records.forEach {
//        println(it.posteriorGround)
//    }
    log.plot(63)
}