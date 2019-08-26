package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.*

abstract class Agent {
    companion object {
        const val GRIDSIZE = Simulation.GRIDSIZE
    }

    protected val pos: Int

    val xPos: Int
        get() = pos.rem(GRIDSIZE)
    val yPos: Int
        get() = pos.div(GRIDSIZE)


    constructor(xPos: Int, yPos: Int) {
        pos = (xPos+GRIDSIZE).rem(GRIDSIZE) + GRIDSIZE*(yPos+GRIDSIZE).rem(GRIDSIZE)
    }

    constructor(id: Int) { pos = id }


    abstract fun copyAt(xPos: Int, yPos: Int): Agent


    fun diffuse(h: HashFockVector<Agent>, rate: Double) {
        h += action(rate/4.0, copyAt(xPos + 1, yPos))
        h += action(rate/4.0, copyAt(xPos - 1, yPos))
        h += action(rate/4.0, copyAt(xPos, yPos + 1))
        h += action(rate/4.0, copyAt(xPos, yPos - 1))
    }


    fun die(h: HashFockVector<Agent>, rate: Double) {
        h += action(rate)
    }

    fun action(rate: Double, vararg addedAgents: Agent) : FockVector<Agent> {
        val from = ActionBasis(mapOf(this to 1),this)
        val to = ActionBasis(addedAgents.asList(), this)
        return (to.toVector() - from.toVector())*rate
    }


    fun interaction(rate: Double, otherAgent: Agent, vararg addedAgents: Agent) : FockVector<Agent> {
        val thisAndOther = listOf(this, otherAgent)
        val from = Basis.newBasis(thisAndOther, thisAndOther)
        val to = Basis.newBasis(addedAgents.asList(), thisAndOther)
        return (to.toVector() - from.toVector())*rate
    }


}