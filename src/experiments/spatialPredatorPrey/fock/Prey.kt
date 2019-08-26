package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.HashFockVector

class Prey : Agent {
    companion object {
        const val rDie = 0.03 // death rate per unit time
        const val rReproduce = 0.06//0.045 // reproduction rate per step
        const val rDiffuse = 1.0 // rate of movement
    }

    constructor(xPos: Int, yPos: Int) : super(xPos, yPos)
    constructor(pos: Int) : super(pos)

    override fun copyAt(xPos: Int, yPos: Int) = Prey(xPos,yPos)

    fun hamiltonian(h: HashFockVector<Agent>) {
        reproduce(h, rReproduce)
        diffuse(h, rDiffuse)
        die(h, rDie)
    }

    fun reproduce(h: HashFockVector<Agent>, rate: Double) {
        h += action(rate/4.0, Prey(xPos + 1, yPos), this)
        h += action(rate/4.0, Prey(xPos - 1, yPos), this)
        h += action(rate/4.0, Prey(xPos, yPos + 1), this)
        h += action(rate/4.0, Prey(xPos, yPos - 1), this)
    }

    override fun toString() = "r($xPos,$yPos)"

    override fun hashCode() = pos

    override fun equals(other: Any?) = (other is Prey && pos == other.pos)
}