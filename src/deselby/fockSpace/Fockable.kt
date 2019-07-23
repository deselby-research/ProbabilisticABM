package deselby.fockSpace

interface Fockable<AGENT> {
    fun create(d : AGENT) : Fockable<AGENT>
    fun create(d : AGENT, n : Int) : Fockable<AGENT>
    fun create(creations : Map<AGENT,Int>) : Fockable<AGENT>
    fun annihilate(d : AGENT) : Fockable<AGENT>
}
