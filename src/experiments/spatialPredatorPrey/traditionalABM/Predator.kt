package experiments.spatialPredatorPrey.traditionalABM

import kotlin.random.Random

class Predator(iPos : Int, jPos : Int) : Agent(iPos, jPos) {
    val gamma = 0.03 // death rate per step
    val beta  = 0.5  // prey capture rate per step per prey
    val delta = 0.4
    val deltaP = delta/beta // reproduction rate per step per prey caught

    override fun step() {
        if(Random.nextDouble() < gamma) {
            Simulation.grid.remove(this)
            return
        }

        hunt(iPos, jPos)
        hunt(right(), jPos)
        hunt(left() , jPos)
        hunt(iPos, up())
        hunt(iPos, down())

        super.step()

    }

    fun hunt(i : Int, j : Int) {
        val prey = Simulation.grid[i,j].filter { agent -> agent is Prey }
        for(p in prey) {
            if(Random.nextDouble() < beta) {
                Simulation.grid.remove(p)
                if(Random.nextDouble() < deltaP) reproduce {i,j -> Predator(i, j) }
            }
        }
    }

}