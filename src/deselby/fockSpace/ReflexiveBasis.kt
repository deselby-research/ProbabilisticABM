package deselby.fockSpace

class ReflexiveBasis<AGENT>(creations: Map<AGENT, Int>, val d: AGENT) : Basis<AGENT>(creations) {

    override fun forEachAnnihilationEntry(entryConsumer: (AGENT, Int) -> Unit) {
        entryConsumer(d,2)
    }

    override fun create(entries: Iterable<Map.Entry<AGENT,Int>>) = ReflexiveBasis(creations * entries, d)

    override fun forEachAnnihilationKey(keyConsumer: (AGENT) -> Unit) {
        keyConsumer(d)
    }

    // ca commuteToPerturbation C = (C^-1)c[a,C]
    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val m = basis.creations[d]?:return
        termConsumer(ActionBasis(creations.times(d,-1), d), 2.0*m)
        if(m>1) termConsumer(CreationBasis(creations.times(d,-2)), (m*(m-1)).toDouble())
    }

//    override fun commutationsTo(termConsumer: (AGENT, Basis<AGENT>, Double) -> Unit) {
//        termConsumer(d, ActionBasis(creationVector, d), 2.0)
//    }

    override fun create(d: AGENT, n: Int): Basis<AGENT> {
        return ReflexiveBasis(creations.times(d,n), d)
    }

    override fun timesAnnihilate(d: AGENT): Basis<AGENT> {
        throw(NotImplementedError())
    }

//    override fun multiplyTo(otherBasis: CreationBasis<AGENT>,
//                            ground: Ground<AGENT>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        val lambda = ground.lambda(d)
//        val nCreations = otherBasis.creations[d]?:run {
//            if(lambda == 0.0) return@multiplyTo
//            0
//        }
//        val creationUnion = this.creations times otherBasis.creations
//        if(lambda != 0.0) {
//            termConsumer(CreationBasis(creationUnion), lambda*lambda)
//            if(nCreations != 0) termConsumer(CreationBasis(creationUnion.times(d,-1)), 2.0*lambda*nCreations)
//        }
//        if(nCreations > 1) {
//            termConsumer(CreationBasis(creationUnion.times(d,-2)), (nCreations*(nCreations-1)).toDouble())
//        }
//    }
//
//
//    override fun multiplyTo(groundBasis: GroundedBasis<AGENT,Ground<AGENT>>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        val lambda = groundBasis.ground.lambda(d)
//        val nCreations = groundBasis.basis.creations[d]?:0
//        if(lambda != 0.0) {
//            termConsumer(CreationBasis(creations), lambda*lambda)
//            if(nCreations != 0) termConsumer(CreationBasis(creations.times(d,-1)), 2.0*lambda*nCreations)
//        }
//        if(nCreations > 1) termConsumer(CreationBasis(creations.times(d,-2)), (nCreations*(nCreations-1)).toDouble())
//    }


    override fun hashCode(): Int {
        return  1922 + creations.hashCode() + d.hashCode()*62
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is ReflexiveBasis<*>) return false
        return (d == other.d && creations == other.creations)
    }


    override fun toString(): String {
        return super.toString() + "a($d)^2"
    }
}