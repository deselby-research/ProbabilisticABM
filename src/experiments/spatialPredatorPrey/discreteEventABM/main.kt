package experiments.spatialPredatorPrey.discreteEventABM

import koma.pow

fun main() {
    val sim = Simulation(200,400)
//    val sim = Simulation(0, 100)
//    sim.plot()
    for(i in 1..4000) {
        sim.simulate(0.1)
//        println("${sim.predatorMultiplicity()} ${sim.preyMultiplicity()} ${sim.averageMultiplicity()}")
//        sim.gp("pause 0.01")
        sim.plot()
    }
}