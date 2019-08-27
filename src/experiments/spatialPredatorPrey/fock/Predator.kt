package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.HashFockVector
import experiments.spatialPredatorPrey.Params

class Predator : Agent {
//    companion object {
//        const val rDie = 0.07 // death rate per unit time
//        const val rCaptureOnly  = 0.05  // prey captureOnly rate per unit time per prey
//        const val rCaptureAndReproduce = 0.5 // reproduction and captureOnly rate per unit time per prey
//        const val rDiffuse = 1.0 // rate of movement
//    }


    constructor(xPos: Int, yPos: Int, gridSize: Int) : super(xPos, yPos, gridSize)
    constructor(pos: Int, gridSize: Int) : super(pos, gridSize)


    override fun copyAt(xPos: Int, yPos: Int) = Predator(xPos, yPos, GRIDSIZE)


    fun hamiltonian(h: HashFockVector<Agent>, params: Params) {
        diffuse(h, params.predDiffuse)
        capture(h, params.predCaptureOnly)
        captureAndReproduce(h, params.predCaptureAndReproduce)
        die(h, params.predDie)
    }

    fun capture(h: HashFockVector<Agent>, rate: Double) {
        h += interaction(rate, Prey(xPos, yPos,GRIDSIZE), this)
        h += interaction(rate, Prey(xPos + 1, yPos,GRIDSIZE), this)
        h += interaction(rate, Prey(xPos - 1, yPos,GRIDSIZE), this)
        h += interaction(rate, Prey(xPos, yPos + 1,GRIDSIZE), this)
        h += interaction(rate, Prey(xPos, yPos - 1,GRIDSIZE), this)
    }

    fun captureAndReproduce(h: HashFockVector<Agent>, rate: Double) {
        h += interaction(rate, Prey(xPos + 1, yPos,GRIDSIZE), this, Predator(xPos+1, yPos,GRIDSIZE))
        h += interaction(rate, Prey(xPos - 1, yPos,GRIDSIZE), this, Predator(xPos-1, yPos,GRIDSIZE))
        h += interaction(rate, Prey(xPos, yPos + 1,GRIDSIZE), this, Predator(xPos, yPos+1,GRIDSIZE))
        h += interaction(rate, Prey(xPos, yPos - 1,GRIDSIZE), this, Predator(xPos, yPos-1,GRIDSIZE))
    }

    override fun toString() = "f($xPos,$yPos)"

    override fun hashCode() = pos + GRIDSIZE*GRIDSIZE

    override fun equals(other: Any?) = (other is Predator && pos == other.pos)
}