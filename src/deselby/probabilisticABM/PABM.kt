package deselby.probabilisticABM

interface PABM<AGENT> {
    fun add(a : AGENT) : PABM<AGENT>
    fun add(a : AGENT, n : Int) : PABM<AGENT>
    fun integrate(t : Double) : PABM<AGENT>
    fun setBehaviour(b : Behaviour<AGENT>)
}