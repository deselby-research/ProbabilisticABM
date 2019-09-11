package experiments.spatialPredatorPrey.generatorPoly

import experiments.spatialPredatorPrey.Agent2D

class PredPreyAgent(val isPredator : Boolean,
                    override var iPos : Int,
                    override var jPos : Int) : Agent2D