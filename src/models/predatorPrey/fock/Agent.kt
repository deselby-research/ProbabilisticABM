package models.predatorPrey.fock

import deselby.fockSpace.*
import java.io.Serializable

abstract class Agent(val pos: Int): Serializable {
//    companion object {
//        const val GRIDSIZE = Simulation.GRIDSIZE
//    }

//    protected val pos: Int = id
//    protected val GRIDSIZE: Int

//    val xPos: Int
//        get() = pos.rem(GRIDSIZE)
//    val yPos: Int
//        get() = pos.div(GRIDSIZE)


//    constructor(xPos: Int, yPos: Int, gridSize: Int) {
////        this.GRIDSIZE = gridSize
//        pos = (xPos+gridSize).rem(gridSize) + gridSize*((yPos+gridSize).rem(gridSize))
//    }

    //    abstract fun copyAt(xPos: Int, yPos: Int): Agent
    abstract fun copyAt(id: Int): Agent



    fun diffuse(h: HashFockVector<Agent>, size: Int, rate: Double) {
        h += action(rate/4.0, copyAt(right(size)))
        h += action(rate/4.0, copyAt(left(size)))
        h += action(rate/4.0, copyAt(up(size)))
        h += action(rate/4.0, copyAt(down(size)))
    }


    fun die(h: HashFockVector<Agent>, rate: Double) {
        h += action(rate)
    }

    fun right(size: Int) = pos - pos.rem(size) + (pos+1).rem(size)
    fun left(size: Int) = pos - pos.rem(size) + (pos+size-1).rem(size)
    fun up(size: Int) = (pos + size).rem(size*size)
    fun down(size: Int) = (pos + size*size - size).rem(size*size)


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


    fun translate(transVector: Int, gridSize: Int): Agent {
        val newX = (pos + transVector + gridSize).rem(gridSize)
        val newY = (transVector.div(gridSize) + pos.div(gridSize) + gridSize).rem(gridSize)
        return copyAt(newY*gridSize + newX)
    }
}