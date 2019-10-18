package models.predatorPrey.fock

import deselby.fockSpace.BinomialBasis

data class Observation(val real: Map<Agent,Int>, val observed: BinomialBasis<Agent>)