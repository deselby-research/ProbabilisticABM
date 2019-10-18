package models.predatorPrey.generatorPoly

interface PABM<AGENT> : MutableCollection<AGENT> {
    fun integrate(t : Double) : PABM<AGENT>
    fun setBehaviour(b : Behaviour<AGENT>)
}