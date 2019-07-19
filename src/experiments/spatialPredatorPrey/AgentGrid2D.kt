package experiments.spatialPredatorPrey

open class AgentGrid2D<T : Agent2D>(iSize : Int, jSize : Int) : AbstractMutableCollection<T>() {
    val iSize : Int
        get() = grid.size
    val jSize : Int
        get() = if(iSize == 0) 0 else grid[0].size

    val grid = Array(iSize) {
        Array(jSize) {
            ArrayList<T>()
        }
    }
    val agents = ArrayList<T>()

    override val size: Int
        get() = agents.size

    override fun iterator(): MutableIterator<T> = AgentGridIterator(this)

    operator fun get(i : Int, j : Int) : ArrayList<T> = grid[i][j]

    fun move(agent : T, i : Int, j : Int, newi : Int, newj : Int) : Boolean {
        if(!get(i,j).remove(agent)) return false
        if(!get(newi, newj).add(agent)) {
            get(i,j).add(agent)
            return false
        }
        return true
    }

    override fun add(agent : T) : Boolean {
        if(!agents.add(agent)) return false
        if(!get(agent.iPos, agent.jPos).add(agent)) {
            agents.remove(agent)
            return false
        }
        return true
    }

    override fun remove(agent : T) : Boolean {
        if(!agents.remove(agent)) return false
        if(!get(agent.iPos, agent.jPos).remove(agent)) {
            agents.add(agent)
            return false
        }
        return true
    }

    class AgentGridIterator<T: Agent2D>(val agentGrid : AgentGrid2D<T>) : MutableIterator<T> {
        private val listIterator = agentGrid.agents.iterator()
        private var last : T? = null

        override fun hasNext() = listIterator.hasNext()

        override fun next() : T {
            val next = listIterator.next()
            last = next
            return next
        }

        override fun remove() {
            listIterator.remove()
            val agent = last!! // is last is null, listIterator.remove() will have failed
            agentGrid[agent.iPos, agent.jPos].remove(agent)
        }

    }
}