package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.HashFockVector

class Predator(xPos: Int, yPos: Int) : Agent(xPos, yPos) {
    companion object {
        const val rDie = 0.03 // death rate per unit time
        const val rCapture  = 0.5  // prey capture rate per unit time per prey
        const val rReproduce = 0.4 // reproduction rate per unit time per prey
        const val rDiffuse = 1.5 // rate of movement
    }

    override fun copyAt(xPos: Int, yPos: Int) = Predator(xPos, yPos)


    fun hamiltonian(h: HashFockVector<Agent>) {
        diffuse(h, rDiffuse)
        hunt(h, rCapture)
        reproduce(h, rReproduce)
        die(h, rDie)
    }

    fun hunt(h: HashFockVector<Agent>, rate: Double) {
        h += interaction(rate, Prey(xPos, yPos), this)
        h += interaction(rate, Prey(xPos + 1, yPos), this)
        h += interaction(rate, Prey(xPos - 1, yPos), this)
        h += interaction(rate, Prey(xPos, yPos + 1), this)
        h += interaction(rate, Prey(xPos, yPos - 1), this)
    }

    fun reproduce(h: HashFockVector<Agent>, rate: Double) {
        h += interaction(rate, Prey(xPos + 1, yPos), this, Predator(xPos+1, yPos))
        h += interaction(rate, Prey(xPos - 1, yPos), this, Predator(xPos-1, yPos))
        h += interaction(rate, Prey(xPos, yPos + 1), this, Predator(xPos, yPos+1))
        h += interaction(rate, Prey(xPos, yPos - 1), this, Predator(xPos, yPos-1))
    }

}