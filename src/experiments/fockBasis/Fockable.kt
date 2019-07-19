package experiments.fockBasis

interface Fockable<AGENT> {
    fun create(d : AGENT) : Fockable<AGENT>
    fun create(d : AGENT, n : Int) : Fockable<AGENT>
    fun annihilate(d : AGENT) : Fockable<AGENT>
}