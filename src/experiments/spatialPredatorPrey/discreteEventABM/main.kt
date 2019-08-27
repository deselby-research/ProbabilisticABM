package experiments.spatialPredatorPrey.discreteEventABM

import deselby.std.Gnuplot
import experiments.spatialPredatorPrey.StandardParams
import koma.pow

fun main() {
    plotObservations(0.1, 10)
//    simulate()
}


fun simulate() {
    val sim = Simulation(StandardParams)
    for(i in 1..100) {
        sim.simulate(0.1)
//        println("${sim.predatorMultiplicity()} ${sim.preyMultiplicity()} ${sim.averageMultiplicity()}")
        sim.gp("pause 0.5")
        sim.plot()
    }
}

fun plotObservations(pObserve: Double, nObservations: Int) {
    val sim = Simulation(StandardParams)
    val observationList = sim.generateObservations(pObserve, nObservations, 1.0)
    for(observation in observationList) {
        val plotData = observation.observed.asSequence().flatMap { (agent,_) ->
            sequenceOf(agent.xPos, agent.yPos, if(agent is Prey) 1 else 2)
        }
        sim.gp("plot [0:${sim.params.GRIDSIZE}][0:${sim.params.GRIDSIZE}] '-' binary record=(${observation.observed.size}) using 1:2:3 with points pointtype 5 pointsize 0.5 lc variable")
        sim.gp.write(plotData)
        sim.gp("pause 1.0")
    }
}