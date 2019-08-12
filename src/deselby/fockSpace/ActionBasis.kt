package deselby.fockSpace

class ActionBasis<AGENT>(val d : AGENT, creations: Map<AGENT,Int> = emptyMap()) : ActBasis<AGENT>(creations) {

    override fun create(d: AGENT, n: Int): ActBasis<AGENT> {
        return ActionBasis(d, creations.plus(d,n))
    }

    override fun annihilate(d: AGENT): ActBasis<AGENT> {
        return if(this.d == d) ReflexiveActBasis(d, creations) else InteractionBasis(this.d, d, creations)
    }

    override fun multiplyTo(otherBasis: ActCreationBasis<AGENT>,
                            ground: GroundState<AGENT>,
                            termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit) {
        val lambda = ground.lambda(d)
        val nCreations = otherBasis.creations[d]
        var creationUnion: Map<AGENT,Int>? = null
        if(lambda != 0.0) {
            creationUnion = this.creations union otherBasis.creations
            termConsumer(ActCreationBasis(creationUnion), lambda)
        }
        if(nCreations != null) {
            creationUnion = creationUnion?.plus(d,-1)?:(this.creations union otherBasis.creations).plus(d,-1)
            termConsumer(ActCreationBasis(creationUnion), nCreations.toDouble())
        }
    }



    override fun multiplyTo(groundBasis: GroundBasis<AGENT>,
                            termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit) {
        val lambda = groundBasis.ground.lambda(d)
        if(lambda != 0.0) termConsumer(ActCreationBasis(creations), lambda)
        val nCreations = groundBasis.basis.creations[d]
        if(nCreations != null) termConsumer(ActCreationBasis(creations.plus(d,-1)), nCreations.toDouble())
    }


    override fun hashCode(): Int {
        return  961 + creations.hashCode() + d.hashCode()*31
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is ActionBasis<*>) return false
        return (d == other.d && creations == other.creations)
    }


    override fun toString(): String {
        return super.toString() + "a($d)"
    }

    //    override fun multiply(otherBasis: ActCreationBasis<AGENT>,
//                            ground: GroundState<AGENT>) : Sequence<Pair<ActCreationBasis<AGENT>,Double>> {
//        val lambda = ground.lambda(d)
//        val nCreations = otherBasis.creations[d]
//        if(lambda != 0.0) {
//            val creationUnion = this.creations union otherBasis.creations
//            if(nCreations != null) {
//                return sequenceOf(
//                        Pair(ActCreationBasis(creationUnion.plus(d, -1)), nCreations.toDouble()),
//                        Pair(ActCreationBasis(creationUnion), lambda)
//                )
//            }
//            return sequenceOf(Pair(ActCreationBasis(creationUnion), lambda))
//        } else if(nCreations != null) {
//            val creationUnion = this.creations union otherBasis.creations
//            return sequenceOf(Pair(ActCreationBasis(creationUnion.plus(d, -1)), nCreations.toDouble()))
//        }
//        return emptySequence()
//    }

}