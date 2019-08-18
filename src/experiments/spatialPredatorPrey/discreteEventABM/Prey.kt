package experiments.spatialPredatorPrey.discreteEventABM

import deselby.std.nextExponential
import deselby.std.nextPoisson

class Prey : Agent {
    companion object {
        const val rDie = 0.03 // death rate per unit time
        const val rReproduce = 0.045 // reproduction rate per step
        const val rDiffuse = 4.0 // rate of movement
        const val totalRate = rDie + rReproduce + rDiffuse
    }

    constructor(x: Int, y: Int) : super(x,y)

    override fun scheduleNextEvent(sim: Simulation) {
        val t = sim.time + sim.rand.nextExponential(totalRate)
        nextEvent = sim.schedule(t, this)
    }

    override fun executeEvent(sim: Simulation) {
        var r = sim.rand.nextDouble()* totalRate

        if(r < rDie) {
            die(sim)
            return
        } else r -= rDie
        if(r < rDiffuse) {
            diffuse(sim)
            return
        } else r -= rDiffuse
        if(r < rReproduce) {
            reproduce(sim)
        }
    }


    fun reproduce(sim: Simulation) {
        when(sim.rand.nextInt(4)) {
            0 -> sim.add(Prey(xPos+1, yPos))
            1 -> sim.add(Prey(xPos-1, yPos))
            2 -> sim.add(Prey(xPos, yPos+1))
            3 -> sim.add(Prey(xPos, yPos-1))
        }
    }

    override fun hashCode(): Int {
        return id
    }

//    override fun equals(other: Any?): Boolean {
//        if(other !is Prey) return false
//        return id == other.id
//    }


}