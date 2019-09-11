package deselby.fockSpace

class MutableCreationBasis<AGENT>(private val mcreations: MutableMap<AGENT,Int>) : CreationBasis<AGENT>(mcreations) {

    constructor(toCopy: CreationBasis<AGENT>) : this(HashMap(toCopy.creations))

    operator fun timesAssign(other: CreationBasis<AGENT>) {
        mcreations *= other.creations
    }


}