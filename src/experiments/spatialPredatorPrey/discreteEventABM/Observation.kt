package experiments.spatialPredatorPrey.discreteEventABM

data class Observation(val real: HashMap<Agent,Int> = HashMap(), val observed: HashMap<Agent,Int> = HashMap())