package experiments.spatialPredatorPrey.generatorPoly

// base class for actions and interactions
abstract class Act<AGENT>(val rate : Double) {
    abstract fun subjectSelector(subj : AGENT) : Boolean
}
