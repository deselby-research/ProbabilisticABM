package experiments.spatialPredatorPrey.discreteEventABM

import deselby.std.extensions.nextExponential

class Predator : Agent {
    companion object {
        const val rDie = 0.07 // death rate per unit time
        const val rCaptureOnly  = 0.05  // prey captureOnly rate per unit time per prey
        const val rCaptureAndReproduce = 0.5 // reproduction rate per unit time per prey
        const val rCapture = rCaptureOnly + rCaptureAndReproduce
        const val rDiffuse = 1.0 // rate of movement
    }

    var totalRate = 0.0
    val preyInRange = ArrayList<Prey>()

    constructor(x: Int, y: Int) : super(x,y)
    constructor(id: Int) : super(id)

    override fun scheduleNextEvent(sim: Simulation) {
        findPreyInRange(sim)
        totalRate = rDie + rDiffuse + rCapture*preyInRange.size
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
            val chosenPrey = preyInRange[(r / rCapture).toInt()]
            if (r.rem(rCapture) < rCaptureOnly)
                captureOnly(chosenPrey, sim)
            else
                captureAndReproduce(chosenPrey, sim)
        }
    }

    fun findPreyInRange(sim: Simulation) {
        preyInRange.clear()
        sim.agentsAt(xPos+1,yPos).forEach { if(it is Prey) preyInRange.add(it) }
        sim.agentsAt(xPos-1,yPos).forEach { if(it is Prey) preyInRange.add(it) }
        sim.agentsAt(xPos,yPos+1).forEach { if(it is Prey) preyInRange.add(it) }
        sim.agentsAt(xPos,yPos-1).forEach { if(it is Prey) preyInRange.add(it) }
    }

    fun captureOnly(prey: Prey, sim: Simulation) { sim.remove(prey) }

    fun captureAndReproduce(prey: Prey, sim: Simulation) {
        sim.remove(prey)
        sim.add(Predator(prey.id))
    }

    override fun toString() = "f($xPos,$yPos)"

    override fun fockId() = hashCode()
    override fun hashCode() = id + Simulation.GRIDSIZE*Simulation.GRIDSIZE
}