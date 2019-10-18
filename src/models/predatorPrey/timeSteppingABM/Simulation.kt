package models.predatorPrey.timeSteppingABM

import models.predatorPrey.AgentGrid2D
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() {
    val plotter = ABMPlotter()

    Simulation.initialise()
    plotter.plot(Simulation.grid)
    plotter("pause mouse any")

    val t = measureTimeMillis {
        for (step in 1..50) {
            Simulation.step()
            if (step.rem(2) == 0) {
            plotter("pause 0.02")
                plotter.plot(Simulation.grid)
            }
        }
    }

    plotter.close()

    println("took $t ms")
}

object Simulation {
    const val ISIZE = 200
    const val JSIZE = 200
    val grid = AgentGrid2D<Agent>(ISIZE, JSIZE)

    fun initialise() {
        for(i in 1..400) {
            grid.add(Predator(Random.nextInt(ISIZE), Random.nextInt(JSIZE)))
        }

        for(i in 1..200) {
            grid.add(Prey(Random.nextInt(ISIZE), Random.nextInt(JSIZE)))
        }
    }

    fun step() {
        val predators = grid.agents.filter { agent -> agent is Predator }
        for (predator in predators) {
            predator.step()
        }
        val prey = grid.agents.filter { agent -> agent is Prey }
        for (p in prey) {
            p.step()
        }
    }
}