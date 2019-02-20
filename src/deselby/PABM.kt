package deselby

interface PABM<AGENT> {
    fun add(a : AGENT) : PABM<AGENT>
    fun integrate(t : Double) : PABM<AGENT>
    fun setBehaviour(b : Behaviour<AGENT>)
}