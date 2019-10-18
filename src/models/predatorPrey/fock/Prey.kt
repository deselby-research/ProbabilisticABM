package models.predatorPrey.fock

import deselby.fockSpace.HashFockVector
import models.predatorPrey.Params

class Prey : Agent {
//    companion object {
//        const val rDie = 0.03 // death rate per unit time
//        const val rReproduce = 0.06//0.045 // reproduction rate per step
//        const val rDiffuse = 1.0 // rate of movement
//    }

//    constructor(xPos: Int, yPos: Int, gridSize: Int) : super(xPos, yPos, gridSize)
    constructor(pos: Int) : super(pos)

    override fun copyAt(pos: Int) = Prey(pos)

    fun hamiltonian(h: HashFockVector<Agent>, params: Params) {
        reproduce(h, params)
        diffuse(h, params.GRIDSIZE, params.preyDiffuse)
        die(h, params.preyDie)
    }

    fun reproduce(h: HashFockVector<Agent>, params: Params) {
        h += action(params.preyReproduce/4.0, Prey(left(params.GRIDSIZE)), this)
        h += action(params.preyReproduce/4.0, Prey(right(params.GRIDSIZE)), this)
        h += action(params.preyReproduce/4.0, Prey(up(params.GRIDSIZE)), this)
        h += action(params.preyReproduce/4.0, Prey(down(params.GRIDSIZE)), this)
    }

    override fun toString() = "r($pos)"

    override fun hashCode() = pos*2

    override fun equals(other: Any?) = (other is Prey && pos == other.pos)
}