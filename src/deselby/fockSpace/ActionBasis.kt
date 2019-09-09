package deselby.fockSpace

class ActionBasis<AGENT>(creations: Map<AGENT, Int>, val d: AGENT) : Basis<AGENT>(creations) {

    constructor(creations: Collection<AGENT>, annihilation: AGENT) : this(creations.toCountMap(), annihilation)

    // ca commuteToPerturbation C = (C^-1)c[a,C]
    // = nCreations c a_d*^(-1)
    // since [a_d, a_d*^nCreations] = nCreations a_d*^(nCreations-1)
    //
    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val m = basis.creations[d]?:return
        termConsumer(CreationBasis(this.creations.times(d,-1)), m.toDouble())
    }

    override fun forEachAnnihilationEntry(entryConsumer: (AGENT, Int) -> Unit) {
        entryConsumer(d,1)
    }

    override fun create(entries: Iterable<Map.Entry<AGENT,Int>>) = ActionBasis(creations * entries, d)

    override fun forEachAnnihilationKey(keyConsumer: (AGENT) -> Unit) {
        keyConsumer(d)
    }

    override fun toAnnihilationMap(): Map<AGENT, Int> {
        return mapOf(d to 1)
    }

//    override fun semicommute(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
//        val nCreations = basis.creations[d]?:return
//        termConsumer(CreationBasis(basis.creations.times(d,-1) times this.creations), nCreations.toDouble())
//    }

//    override fun commutationsTo(termConsumer: (AGENT, Basis<AGENT>, Double) -> Unit) {
//        termConsumer(d, CreationBasis(creationVector), 1.0)
//    }

    override fun create(d: AGENT, n: Int): Basis<AGENT> {
        return ActionBasis(creations.times(d,n), d)
    }

    override fun timesAnnihilate(d: AGENT): Basis<AGENT> {
        return if(this.d == d) ReflexiveBasis(creations, d) else InteractionBasis(creations, this.d, d)
    }

//    override fun multiplyTo(otherBasis: CreationBasis<AGENT>,
//                            ground: Ground<AGENT>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        val lambda = ground.lambda(d)
//        val nCreations = otherBasis.creations[d]
//        var creationUnion: Map<AGENT,Int>? = null
//        if(lambda != 0.0) {
//            creationUnion = this.creations times otherBasis.creations
//            termConsumer(CreationBasis(creationUnion), lambda)
//        }
//        if(nCreations != null) {
//            creationUnion = creationUnion?.times(d,-1)?:(this.creations times otherBasis.creations).times(d,-1)
//            termConsumer(CreationBasis(creationUnion), nCreations.toDouble())
//        }
//    }
//
//
//
//    override fun multiplyTo(groundBasis: GroundedBasis<AGENT,Ground<AGENT>>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        val lambda = groundBasis.ground.lambda(d)
//        if(lambda != 0.0) termConsumer(CreationBasis(creations), lambda)
//        val nCreations = groundBasis.basis.creations[d]
//        if(nCreations != null) termConsumer(CreationBasis(creations.times(d,-1)), nCreations.toDouble())
//    }


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

    //    override fun multiply(otherBasis: CreationBasis<AGENT>,
//                            ground: Ground<AGENT>) : Sequence<Pair<CreationBasis<AGENT>,Double>> {
//        val lambda = ground.lambda(d)
//        val nCreations = otherBasis.creationVector[d]
//        if(lambda != 0.0) {
//            val creationUnion = this.creationVector times otherBasis.creationVector
//            if(nCreations != null) {
//                return sequenceOf(
//                        Pair(CreationBasis(creationUnion.times(d, -1)), nCreations.toDouble()),
//                        Pair(CreationBasis(creationUnion), lambda)
//                )
//            }
//            return sequenceOf(Pair(CreationBasis(creationUnion), lambda))
//        } else if(nCreations != null) {
//            val creationUnion = this.creationVector times otherBasis.creationVector
//            return sequenceOf(Pair(CreationBasis(creationUnion.times(d, -1)), nCreations.toDouble()))
//        }
//        return emptySequence()
//    }

}