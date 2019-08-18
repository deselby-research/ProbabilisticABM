package experiments.spatialPredatorPrey.discreteEventABM

import deselby.std.nextExponential
import deselby.std.nextPoisson
import kotlin.math.floor

class Predator : Agent {
    companion object {
        const val rDie = 0.03 // death rate per unit time
        const val rCapture  = 0.4  // prey capture rate per unit time per prey
        const val rReproduce = 0.4 // reproduction rate per unit time per prey
        const val rDiffuse = 3.0 // rate of movement
    }

    var totalRate = 0.0
    val preyInRange = ArrayList<Prey>()

    constructor(x: Int, y: Int) : super(x,y)
    constructor(id: Int) : super(id)

    override fun scheduleNextEvent(sim: Simulation) {
        getPreyInRange(sim)
        totalRate = rDie + rDiffuse + (rCapture + rReproduce)*preyInRange.size
        val nextEventTime = sim.time + sim.rand.nextExponential(totalRate)
        nextEvent = sim.schedule(nextEventTime, this)
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
        if(preyInRange.size > 0) {
            val chosenPrey = preyInRange[(r / (rCapture + rReproduce)).toInt()]
            if (r.rem(rCapture + rReproduce) < rCapture)
                capture(chosenPrey, sim)
            else
                reproduce(chosenPrey, sim)
        }
    }

    fun getPreyInRange(sim: Simulation) {
        preyInRange.clear()
        sim.agentsAt(xPos+1,yPos).forEach { if(it is Prey) preyInRange.add(it) }
        sim.agentsAt(xPos-1,yPos).forEach { if(it is Prey) preyInRange.add(it) }
        sim.agentsAt(xPos,yPos+1).forEach { if(it is Prey) preyInRange.add(it) }
        sim.agentsAt(xPos,yPos-1).forEach { if(it is Prey) preyInRange.add(it) }
    }

    fun capture(prey: Prey, sim: Simulation) {
        sim.remove(prey)
    }

    fun reproduce(prey: Prey, sim: Simulation) {
        sim.add(Predator(prey.id))
    }

    override fun hashCode(): Int {
        return id + Simulation.GRIDSIZE*Simulation.GRIDSIZE
    }


//    override fun equals(other: Any?): Boolean {
//        if(other !is Predator) return false
//        return id == other.id
//    }

}