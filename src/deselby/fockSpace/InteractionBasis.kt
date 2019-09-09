package deselby.fockSpace

class InteractionBasis<AGENT> : Basis<AGENT> {

    val d1: AGENT
    val d2: AGENT

    constructor(creations: Map<AGENT, Int>, d1: AGENT, d2: AGENT) : super(creations) {
        if(d1 == d2) throw(IllegalArgumentException("InteractionBasis should involve different agents. Use ReflexiveBasis instead"))
        this.d1 = d1
        this.d2 = d2
    }

    override fun forEachAnnihilationEntry(entryConsumer: (AGENT, Int) -> Unit) {
        entryConsumer(d1,1)
        entryConsumer(d2,1)
    }

    override fun create(entries: Iterable<Map.Entry<AGENT,Int>>) = InteractionBasis(creations * entries, d1, d2)

    override fun forEachAnnihilationKey(keyConsumer: (AGENT) -> Unit) {
        keyConsumer(d1)
        keyConsumer(d2)
    }

    // ca commuteToPerturbation C = (C^-1)c[a,C]
    override fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val m1 = basis.creations[d1]
        val m2 = basis.creations[d2]
        if(m1 != null) {
            if(m2 != null) {
//                val creationUnion = this.creations times basis.creations
                val creationsminusd1 = creations.times(d1,-1)
                termConsumer(ActionBasis(creationsminusd1, d2), m1.toDouble())
                termConsumer(ActionBasis(creations.times(d2,-1), d1), m2.toDouble())
                termConsumer(CreationBasis(creationsminusd1.times(d2,-1)), (m1*m2).toDouble())
            } else {
                termConsumer(ActionBasis(this.creations.times(d1,-1), d2), m1.toDouble())
            }
        } else if(m2 != null) {
            termConsumer(ActionBasis(this.creations.times(d2,-1), d1), m2.toDouble())
        }
    }

//    override fun commutationsTo(termConsumer: (AGENT, Basis<AGENT>, Double) -> Unit) {
//        termConsumer(d1, ActionBasis(creationVector, d2), 1.0)
//        termConsumer(d2, ActionBasis(creationVector, d1), 1.0)
//    }



    override fun create(d: AGENT, n: Int): Basis<AGENT> {
        return InteractionBasis(creations.times(d,n), d1, d2)
    }

    override fun timesAnnihilate(d: AGENT): Basis<AGENT> {
        val annihilations = HashMap<AGENT,Int>()
        annihilations[d1] = 1
        annihilations[d2] = 1
        annihilations.timesAssign(d, 1)
        return OperatorBasis(creations, annihilations)
    }

//    override fun multiplyTo(otherBasis: CreationBasis<AGENT>,
//                            ground: Ground<AGENT>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        val lambda1 = ground.lambda(d1)
//        val lambda2 = ground.lambda(d2)
//        val n1 = otherBasis.creations[d1]?.toDouble()?:0.0
//        val n2 = otherBasis.creations[d2]?.toDouble()?:0.0
//        // hoping these will get translated to AVX vector instructions!
//        val cll = lambda1*lambda2
//        val cnl = n1*lambda2
//        val cln = lambda1*n2
//        val cnn = n1*n2
//        var cminusd12 : Map<AGENT,Int>? = null
//        val creationUnion = this.creations times otherBasis.creations
//        if(cll != 0.0) termConsumer(CreationBasis(creationUnion), cll)
//        if(cnl != 0.0) {
//            val cminusd1 = creationUnion.times(d1,-1)
//            termConsumer(CreationBasis(cminusd1),cnl)
//            cminusd12 = cminusd1.times(d2,-1)
//        }
//        if(cln != 0.0) {
//            val cminusd2 = creationUnion.times(d2,-1)
//            termConsumer(CreationBasis(cminusd2),cnl)
//            if(cminusd12 == null) cminusd12 = cminusd2.times(d1,-1)
//        }
//        if(cnn != 0.0) {
//            if(cminusd12 == null) cminusd12 = creationUnion.times(d1 to -1, d2 to -1)
//            termConsumer(CreationBasis(cminusd12),cnl)
//        }
//    }
//
//
//    override fun multiplyTo(groundBasis: GroundedBasis<AGENT,Ground<AGENT>>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
//        val lambda1 = groundBasis.ground.lambda(d1)
//        val lambda2 = groundBasis.ground.lambda(d2)
//        val n1 = groundBasis.basis.creations[d1]?.toDouble()?:0.0
//        val n2 = groundBasis.basis.creations[d2]?.toDouble()?:0.0
//        val cll = lambda1*lambda2
//        val cnl = n1*lambda2
//        val cln = lambda1*n2
//        val cnn = n1*n2
//        var cminusd12 : Map<AGENT,Int>? = null
//        if(cll != 0.0) termConsumer(CreationBasis(creations), cll)
//        if(cnl != 0.0) {
//            val cminusd1 = creations.times(d1,-1)
//            termConsumer(CreationBasis(cminusd1),cnl)
//            cminusd12 = cminusd1.times(d2,-1)
//        }
//        if(cln != 0.0) {
//            val cminusd2 = creations.times(d2,-1)
//            termConsumer(CreationBasis(cminusd2),cnl)
//            if(cminusd12 == null) cminusd12 = cminusd2.times(d1,-1)
//        }
//        if(cnn != 0.0) {
//            if(cminusd12 == null) cminusd12 = creations.times(d1 to -1, d2 to -1)
//            termConsumer(CreationBasis(cminusd12),cnl)
//        }
//    }


    override fun hashCode(): Int {
        return  1922 + creations.hashCode() + (d1.hashCode() + d2.hashCode())*31
    }


    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is InteractionBasis<*>) return false
        return (((d1 == other.d1 && d2 == other.d2) || (d1 == other.d2 && d2 == other.d1))
                && creations == other.creations)
    }


    override fun toString(): String {
        return super.toString() + "a($d1)a($d2)"
    }
}