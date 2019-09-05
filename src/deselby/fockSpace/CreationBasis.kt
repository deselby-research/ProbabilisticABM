package deselby.fockSpace

import deselby.std.vectorSpace.OneHotDoubleVector

open class CreationBasis<AGENT>(creations: Map<AGENT, Int> = emptyMap()) : Basis<AGENT>(creations) {

    override fun forEachAnnihilationEntry(entryConsumer: (AGENT, Int) -> Unit) {
    }

    override fun create(entries: Iterable<Map.Entry<AGENT,Int>>) = CreationBasis(creations union entries)

    override fun forEachAnnihilationKey(keyConsumer: (AGENT) -> Unit) { }

//    override fun commutationsTo(termConsumer: (AGENT, Basis<AGENT>, Double) -> Unit) { } // zero

    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) { } // zero

    inline fun commute(basis: ActionBasis<AGENT>, crossinline termConsumer: (Basis<AGENT>, Double) -> Unit) =
        basis.commute(this) { basis, weight -> termConsumer(basis, -weight) }


    override fun create(d: AGENT, n: Int): CreationBasis<AGENT> {
        return CreationBasis(creations.plus(d,n))
    }

    override fun timesAnnihilate(d: AGENT): ActionBasis<AGENT> {
        return ActionBasis(creations, d)
    }

    fun<GROUND: Ground<AGENT>> asGroundedBasis(ground: GROUND) = GroundedBasis(this, ground)

    override fun toAnnihilationMap(): Map<AGENT, Int> {
        return emptyMap()
    }

    override fun operatorUnion(other: Basis<AGENT>): Basis<AGENT> {
        return newBasis(this.creations union other.creations, other.toAnnihilationMap())
    }

//    override fun multiplyTo(otherBasis: CreationBasis<AGENT>,
//                            ground: Ground<AGENT>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        termConsumer(CreationBasis(this.creations union otherBasis.creations),1.0)
//    }
//
//    override fun multiplyTo(groundBasis: GroundedBasis<AGENT,Ground<AGENT>>,
//                        termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        termConsumer(this, 1.0)
//    }

    operator fun get(d : AGENT) : Int {
        return creations[d]?:0
    }

    operator fun div(other: CreationBasis<AGENT>) : CreationBasis<AGENT> {
        val newCreations = HashMap(this.creations)
        other.creations.forEach {
            newCreations.plusAssign(it.key, -it.value)
        }
        return(CreationBasis(newCreations))
    }

    inline operator fun times(other: Basis<AGENT>) = other.create(creations.entries)

    fun toCreationVector(weight: Double = 1.0) : CreationVector<AGENT> = OneHotDoubleVector(this, weight)

    fun toMutableCreationBasis() = MutableCreationBasis(this)

    override fun hashCode(): Int {
        return creations.hashCode()
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is CreationBasis<*>) return false
        return (creations == other.creations)
    }


}