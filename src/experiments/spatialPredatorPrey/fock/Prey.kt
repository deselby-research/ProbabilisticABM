package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.HashFockVector

class Prey(xPos: Int, yPos: Int) : Agent(xPos, yPos) {
    companion object {
        const val rDie = 0.03 // death rate per unit time
        const val rReproduce = 0.04 // reproduction rate per step
        const val rDiffuse = 1.0 // rate of movement
    }

    override fun copyAt(xPos: Int, yPos: Int) = Prey(xPos,yPos)

    fun hamiltonian(h: HashFockVector<Agent>) {
        reproduce(h, rReproduce)
        diffuse(h, rDiffuse)
        die(h, rDie)
    }

    fun reproduce(h: HashFockVector<Agent>, rate: Double) {
        h += action(rate, copyAt(xPos + 1, yPos), this)
        h += action(rate, copyAt(xPos - 1, yPos), this)
        h += action(rate, copyAt(xPos, yPos + 1), this)
        h += action(rate, copyAt(xPos, yPos - 1), this)
    }

}