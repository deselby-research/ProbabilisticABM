package models.predatorPrey.timeSteppingABM

import deselby.std.Gnuplot
import models.predatorPrey.AgentGrid2D
import java.io.OutputStream

class ABMPlotter(persist: Boolean = true, pipeOutputTo: OutputStream = System.out, pipeErrTo: OutputStream = System.err) : Gnuplot(persist, pipeOutputTo, pipeErrTo) {

    init {
        this("set linetype 1 lc 'red'")
        this("set linetype 2 lc 'blue'")
    }

    fun <T : Agent> plot(grid : AgentGrid2D<T>) {
        val data = grid.agents.asSequence().flatMap { agent ->
            sequenceOf(agent.iPos.toFloat(), agent.jPos.toFloat(), if(agent is Predator) 1.0f else 2.0f)
        }
        invoke("plot [0:${Simulation.ISIZE}][0:${Simulation.JSIZE}] '-' binary record=(${grid.agents.size}) using 1:2:3 with points pointtype 5 pointsize 0.5 lc variable")
        write(data)
    }
}