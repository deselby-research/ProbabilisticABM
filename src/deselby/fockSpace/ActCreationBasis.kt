package deselby.fockSpace

import deselby.std.vectorSpace.MutableDoubleVector

open class ActCreationBasis<AGENT>(creations: Map<AGENT, Int> = emptyMap()) : ActBasis<AGENT>(creations) {
    override fun create(d: AGENT, n: Int): ActBasis<AGENT> {
        return ActCreationBasis(creations.plus(d,n))
    }

    override fun annihilate(d: AGENT): ActBasis<AGENT> {
        return ActionBasis(d, creations)
    }

    override fun multiplyTo(otherBasis: ActCreationBasis<AGENT>,
                        ground: GroundState<AGENT>,
                        termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit) {
        termConsumer(ActCreationBasis(this.creations union otherBasis.creations),1.0)
    }

    override fun multiplyTo(groundBasis: GroundBasis<AGENT>,
                        termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit) {
        termConsumer(this, 1.0)
    }


    override fun hashCode(): Int {
        return creations.hashCode()
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is ActCreationBasis<*>) return false
        return (creations == other.creations)
    }


}