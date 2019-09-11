package experiments.spatialPredatorPrey.timeSteppingABM

import kotlin.random.Random

class Prey(iPos : Int, jPos : Int) : Agent(iPos, jPos) {
    val alpha = 0.04 // reproduction rate per step

    override fun step() {
        if(Random.nextDouble() < alpha) reproduce {i,j -> Prey(i, j) }
        super.step()
    }
}