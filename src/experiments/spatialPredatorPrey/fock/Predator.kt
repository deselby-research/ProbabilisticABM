package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.HashFockVector
import experiments.spatialPredatorPrey.Params
import java.io.Serializable

class Predator : Agent {
//    companion object {
//        const val rDie = 0.07 // death rate per unit time
//        const val rCaptureOnly  = 0.05  // prey captureOnly rate per unit time per prey
//        const val rCaptureAndReproduce = 0.5 // reproduction and captureOnly rate per unit time per prey
//        const val rDiffuse = 1.0 // rate of movement
//    }


//    constructor(xPos: Int, yPos: Int, gridSize: Int) : super(xPos, yPos, gridSize)
    constructor(pos: Int) : super(pos)


    override fun copyAt(pos: Int) = Predator(pos)


    fun hamiltonian(h: HashFockVector<Agent>, params: Params) {
        if(params.predDiffuse > 0.0) diffuse(h, params.GRIDSIZE, params.predDiffuse)
        if(params.predCaptureOnly > 0.0) capture(h, params)
        if(params.predCaptureAndReproduce > 0.0) captureAndReproduce(h, params)
        if(params.predDie > 0.0) die(h, params.predDie)
    }

    fun capture(h: HashFockVector<Agent>, params: Params) {
        h += interaction(params.predCaptureOnly, Prey(pos), this)
        h += interaction(params.predCaptureOnly, Prey(right(params.GRIDSIZE)), this)
        h += interaction(params.predCaptureOnly, Prey(left(params.GRIDSIZE)), this)
        h += interaction(params.predCaptureOnly, Prey(up(params.GRIDSIZE)), this)
        h += interaction(params.predCaptureOnly, Prey(down(params.GRIDSIZE)), this)
    }

    fun captureAndReproduce(h: HashFockVector<Agent>, params: Params) {
        h += interaction(params.predCaptureAndReproduce, Prey(right(params.GRIDSIZE)), this, Predator(right(params.GRIDSIZE)))
        h += interaction(params.predCaptureAndReproduce, Prey(left(params.GRIDSIZE)), this, Predator(left(params.GRIDSIZE)))
        h += interaction(params.predCaptureAndReproduce, Prey(up(params.GRIDSIZE)), this, Predator(up(params.GRIDSIZE)))
        h += interaction(params.predCaptureAndReproduce, Prey(down(params.GRIDSIZE)), this, Predator(down(params.GRIDSIZE)))
    }

    override fun toString() = "f($pos)"

    override fun hashCode() = pos*2 + 1

    override fun equals(other: Any?) = (other is Predator && pos == other.pos)
}