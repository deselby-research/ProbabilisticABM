package experiments.spatialPredatorPrey.generatorPoly

interface PABM<AGENT> : MutableCollection<AGENT> {
    fun integrate(t : Double) : PABM<AGENT>
    fun setBehaviour(b : Behaviour<AGENT>)
}