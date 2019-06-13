package experiments.SpatialPredatorPrey.generatorPoly

open class Act<ABM : Collection<AGENT>, AGENT>(val rate : Double, val subjectSelector : (AGENT) -> Boolean) {
}