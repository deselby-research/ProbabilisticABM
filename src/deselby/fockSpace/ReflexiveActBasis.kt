package deselby.fockSpace

class ReflexiveActBasis<AGENT>(val d: AGENT, creations: Map<AGENT, Int>) : ActBasis<AGENT>(creations) {
    override fun create(d: AGENT, n: Int): ActBasis<AGENT> {
        return ReflexiveActBasis(d,creations.plus(d,n))
    }

    override fun annihilate(d: AGENT): ActBasis<AGENT> {
        throw(NotImplementedError())
    }

    override fun multiplyTo(otherBasis: ActCreationBasis<AGENT>,
                            ground: GroundState<AGENT>,
                            termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit) {
        val lambda = ground.lambda(d)
        val nCreations = otherBasis.creations[d]?:run {
            if(lambda == 0.0) return@multiplyTo
            0
        }
        val creationUnion = this.creations union otherBasis.creations
        if(lambda != 0.0) {
            termConsumer(ActCreationBasis(creationUnion), lambda*lambda)
            if(nCreations != 0) termConsumer(ActCreationBasis(creationUnion.plus(d,-1)), 2.0*lambda*nCreations)
        }
        if(nCreations > 1) {
            termConsumer(ActCreationBasis(creationUnion.plus(d,-2)), (nCreations*(nCreations-1)).toDouble())
        }
    }


    override fun multiplyTo(groundBasis: GroundBasis<AGENT>,
                            termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit) {
        val lambda = groundBasis.ground.lambda(d)
        val nCreations = groundBasis.basis.creations[d]?:0
        if(lambda != 0.0) {
            termConsumer(ActCreationBasis(creations), lambda*lambda)
            if(nCreations != 0) termConsumer(ActCreationBasis(creations.plus(d,-1)), 2.0*lambda*nCreations)
        }
        if(nCreations > 1) termConsumer(ActCreationBasis(creations.plus(d,-2)), (nCreations*(nCreations-1)).toDouble())
    }


    override fun hashCode(): Int {
        return  1922 + creations.hashCode() + d.hashCode()*62
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is ReflexiveActBasis<*>) return false
        return (d == other.d && creations == other.creations)
    }


    override fun toString(): String {
        return super.toString() + "a($d^2)"
    }
}