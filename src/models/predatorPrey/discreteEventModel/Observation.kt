package models.predatorPrey.discreteEventModel

data class Observation(val real: HashMap<Agent,Int> = HashMap(), val observed: HashMap<Agent,Int> = HashMap())