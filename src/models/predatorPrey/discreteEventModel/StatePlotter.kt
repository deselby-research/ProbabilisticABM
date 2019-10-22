package models.predatorPrey.discreteEventModel

import deselby.std.Gnuplot

class StatePlotter {
    val gp = Gnuplot()

    constructor() {
        gp("set linetype 1 lc 'red'")
        gp("set linetype 2 lc 'blue'")
    }

    fun plot(sim: Simulation) {
        val data = sim.eventQueue.asSequence().flatMap { (_, agent) ->
            sequenceOf(agent.xPos, agent.yPos, if(agent is Prey) 1 else 2) }.toList()

        gp("plot [0:${sim.params.GRIDSIZE}][0:${sim.params.GRIDSIZE}] '-' binary record=(${data.size/3}) using 1:2:3 with points pointtype 5 pointsize 0.5 lc variable")
        gp.write(data)
    }

}