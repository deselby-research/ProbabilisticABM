package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.HashFockVector

class Predator : Agent {
    companion object {
        const val rDie = 0.07 // death rate per unit time
        const val rCaptureOnly  = 0.05  // prey captureOnly rate per unit time per prey
        const val rCaptureAndReproduce = 0.5 // reproduction and captureOnly rate per unit time per prey
        const val rDiffuse = 1.0 // rate of movement
    }


    constructor(xPos: Int, yPos: Int) : super(xPos, yPos)
    constructor(pos: Int) : super(pos)


    override fun copyAt(xPos: Int, yPos: Int) = Predator(xPos, yPos)


    fun hamiltonian(h: HashFockVector<Agent>) {
        diffuse(h, rDiffuse)
        capture(h, rCaptureOnly)
        captureAndReproduce(h, rCaptureAndReproduce)
        die(h, rDie)
    }

    fun capture(h: HashFockVector<Agent>, rate: Double) {
        h += interaction(rate, Prey(xPos, yPos), this)
        h += interaction(rate, Prey(xPos + 1, yPos), this)
        h += interaction(rate, Prey(xPos - 1, yPos), this)
        h += interaction(rate, Prey(xPos, yPos + 1), this)
        h += interaction(rate, Prey(xPos, yPos - 1), this)
    }

    fun captureAndReproduce(h: HashFockVector<Agent>, rate: Double) {
        h += interaction(rate, Prey(xPos + 1, yPos), this, Predator(xPos+1, yPos))
        h += interaction(rate, Prey(xPos - 1, yPos), this, Predator(xPos-1, yPos))
        h += interaction(rate, Prey(xPos, yPos + 1), this, Predator(xPos, yPos+1))
        h += interaction(rate, Prey(xPos, yPos - 1), this, Predator(xPos, yPos-1))
    }

    override fun toString() = "f($xPos,$yPos)"

    override fun hashCode() = pos + Simulation.GRIDSIZE*Simulation.GRIDSIZE

    override fun equals(other: Any?) = (other is Predator && pos == other.pos)
}