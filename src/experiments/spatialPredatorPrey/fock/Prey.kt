package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.HashFockVector
import experiments.spatialPredatorPrey.Params

class Prey : Agent {
//    companion object {
//        const val rDie = 0.03 // death rate per unit time
//        const val rReproduce = 0.06//0.045 // reproduction rate per step
//        const val rDiffuse = 1.0 // rate of movement
//    }

    constructor(xPos: Int, yPos: Int, gridSize: Int) : super(xPos, yPos, gridSize)
    constructor(pos: Int, gridSize: Int) : super(pos, gridSize)

    override fun copyAt(xPos: Int, yPos: Int) = Prey(xPos,yPos,GRIDSIZE)

    fun hamiltonian(h: HashFockVector<Agent>, params: Params) {
        reproduce(h, params.preyReproduce)
        diffuse(h, params.preyDiffuse)
        die(h, params.preyDie)
    }

    fun reproduce(h: HashFockVector<Agent>, rate: Double) {
        h += action(rate/4.0, Prey(xPos + 1, yPos,GRIDSIZE), this)
        h += action(rate/4.0, Prey(xPos - 1, yPos,GRIDSIZE), this)
        h += action(rate/4.0, Prey(xPos, yPos + 1,GRIDSIZE), this)
        h += action(rate/4.0, Prey(xPos, yPos - 1,GRIDSIZE), this)
    }

    override fun toString() = "r($xPos,$yPos)"

    override fun hashCode() = pos

    override fun equals(other: Any?) = (other is Prey && pos == other.pos)
}