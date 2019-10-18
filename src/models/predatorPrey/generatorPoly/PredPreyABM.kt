package models.predatorPrey.generatorPoly

import models.predatorPrey.AgentGrid2D
import kotlin.random.Random

class PredPreyABM : AgentGrid2D<PredPreyAgent>(128,128) {
    init {
        for(i in 1..400) {
            add(PredPreyAgent(true, Random.nextInt(iSize), Random.nextInt(jSize)))
        }

        for(i in 1..200) {
            add(PredPreyAgent(true, Random.nextInt(iSize), Random.nextInt(jSize)))
        }
    }


}
