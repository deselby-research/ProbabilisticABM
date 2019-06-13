package experiments.SpatialPredatorPrey.traditionalABM

import deselby.std.Array2D

class AgentGrid2D<T : Agent>(iSize : Int, jSize : Int) : Array2D<ArrayList<T>>(
        Array(iSize) { i ->
            Array(jSize) {
                ArrayList<T>()
            }
        }) {

    val agents = ArrayList<T>()

    operator fun get(i : Int, j : Int) : ArrayList<T> = grid[i][j]

    fun move(agent : T, i : Int, j : Int, newi : Int, newj : Int) {
        get(i,j).remove(agent)
        get(newi, newj).add(agent)
    }

    fun add(agent : T) {
        get(agent.iPos, agent.jPos).add(agent)
        agents.add(agent)
    }

    fun remove(agent : T) {
        get(agent.iPos, agent.jPos).remove(agent)
        agents.remove(agent)
    }

}