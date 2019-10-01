package deselby.fockSpace

import deselby.std.vectorSpace.OneHotDoubleVector
import java.io.Serializable

open class CreationBasis<AGENT>(creations: Map<AGENT, Int> = emptyMap()) : Basis<AGENT>(creations), Serializable {
    val hashCache: Int = creations.hashCode()

    override fun create(entries: Iterable<Map.Entry<AGENT,Int>>) = CreationBasis(creations * entries)


//    override fun commutationsTo(termConsumer: (AGENT, Basis<AGENT>, Double) -> Unit) { } // zero

    // ca commuteToPerturbation C = (C^-1)c[a,C]
    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) { } // zero

    inline fun commute(basis: ActionBasis<AGENT>, crossinline termConsumer: (Basis<AGENT>, Double) -> Unit) =
        basis.semicommute(this) { basis, weight -> termConsumer(basis, -weight) }


    override fun create(d: AGENT, n: Int): CreationBasis<AGENT> {
        return CreationBasis(creations.times(d,n))
    }

    override fun timesAnnihilate(d: AGENT): ActionBasis<AGENT> {
        return ActionBasis(creations, d)
    }

    fun<GROUND: Ground<AGENT>> asGroundedBasis(ground: GROUND) = GroundedBasis(this, ground)


    override fun union(other: Basis<AGENT>): Basis<AGENT> {
        return newBasis(this.creations * other.creations, other.annihilations)
    }

    fun union(other: CreationBasis<AGENT>): CreationBasis<AGENT> {
        return CreationBasis(this.creations * other.creations)
    }

//    override fun multiplyTo(otherBasis: CreationBasis<AGENT>,
//                            ground: Ground<AGENT>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        termConsumer(CreationBasis(this.creations times otherBasis.creations),1.0)
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
            newCreations.timesAssign(it.key, -it.value)
        }
        return(CreationBasis(newCreations))
    }

//    inline operator fun times(other: Basis<AGENT>) = other.create(creations.entries)

    fun marginalise(activeAgents: Set<AGENT>): CreationBasis<AGENT> {
        return CreationBasis(creations.filter {activeAgents.contains(it.key)})
    }

    fun toCreationVector(weight: Double = 1.0) : CreationVector<AGENT> = OneHotDoubleVector(this, weight)

    fun toMutableCreationBasis() = MutableCreationBasis(this)

    override fun hashCode(): Int {
        return hashCache
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is CreationBasis<*>) return false
        return (creations == other.creations)
    }


}