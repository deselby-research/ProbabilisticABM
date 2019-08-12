package deselby.fockSpace

class MutableActCreationBasis<AGENT>(private val mcreations: MutableMap<AGENT,Int>) : ActCreationBasis<AGENT>(mcreations) {

}