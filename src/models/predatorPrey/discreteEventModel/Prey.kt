package models.predatorPrey.discreteEventModel

import deselby.std.extensions.nextExponential

class Prey : Agent {
//    companion object {
//        const val rDie = 0.03 // death rate per unit time
//        const val rReproduce = 0.06 // reproduction rate per step
//        const val rDiffuse = 1.0 // rate of movement
//        const val totalRate = rDie + rReproduce + rDiffuse
//    }

    constructor(x: Int, y: Int, gridSize: Int) : super(x,y,gridSize)
    constructor(id: Int, gridSize: Int) : super(id,gridSize)

    override fun scheduleNextEvent(sim: Simulation) {
        val t = sim.time + sim.rand.nextExponential(sim.params.preyTotal)
        nextEvent = sim.schedule(t, this)
    }

    override fun executeEvent(sim: Simulation) {
        var r = sim.rand.nextDouble()* sim.params.preyTotal

        if(r < sim.params.preyDie) {
            die(sim)
            return
        } else r -= sim.params.preyDie
        if(r < sim.params.preyDiffuse) {
            diffuse(sim)
            return
        } else r -= sim.params.preyDiffuse
        if(r < sim.params.preyReproduce) {
            reproduce(sim)
        }
    }


    fun reproduce(sim: Simulation) {
        when(sim.rand.nextInt(4)) {
            0 -> sim.add(Prey(xPos + 1, yPos, sim.params.GRIDSIZE))
            1 -> sim.add(Prey(xPos - 1, yPos, sim.params.GRIDSIZE))
            2 -> sim.add(Prey(xPos, yPos + 1, sim.params.GRIDSIZE))
            3 -> sim.add(Prey(xPos, yPos - 1, sim.params.GRIDSIZE))
        }
    }

    override fun copy() = Prey(id, SIZE)

    override fun toString() = "r($xPos,$yPos)"

//    override fun fockId() = hashCode()
    override fun hashCode() = id*2
}