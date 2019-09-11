package experiments.spatialPredatorPrey.timeSteppingABM

import experiments.spatialPredatorPrey.Agent2D
import kotlin.random.Random

open class Agent(override var iPos : Int,
                 override var jPos : Int) : Agent2D {

    fun move(newiPos : Int, newjPos : Int) {
        Simulation.grid.move(this, iPos, jPos, newiPos, newjPos)
        iPos = newiPos
        jPos = newjPos
    }


    open fun step() {
        when(Random.nextInt(4)) {
            0 -> move(right(), jPos)
            1 -> move(left(), jPos)
            2 -> move(iPos, up())
            3 -> move(iPos, down())
        }
    }

    fun reproduce(factory : (Int, Int) -> Agent) {
        Simulation.grid.add(
                when (Random.nextInt(4)) {
                    0 -> factory(right(), jPos)
                    1 -> factory(left(), jPos)
                    2 -> factory(iPos, up())
                    else -> factory(iPos, down())
                }
        )

    }

    fun right() = (iPos + 1).rem(Simulation.grid.iSize)
    fun left() = (iPos + Simulation.grid.iSize - 1).rem(Simulation.grid.iSize)
    fun up() = (jPos + 1).rem(Simulation.grid.jSize)
    fun down() = (jPos + Simulation.grid.jSize - 1).rem(Simulation.grid.jSize)

}