package deselby.fockSpace

interface FockElement<AGENT, out BASIS : FockElement<AGENT,BASIS>> {
    fun create(d : AGENT) : BASIS
    fun create(d : AGENT, n : Int) : BASIS
    fun create(creations : Map<AGENT,Int>) : BASIS
    fun annihilate(d : AGENT) : BASIS
}