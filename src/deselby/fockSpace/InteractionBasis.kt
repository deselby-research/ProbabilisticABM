package deselby.fockSpace

class InteractionBasis<AGENT>(val d1: AGENT, val d2: AGENT, creations: Map<AGENT, Int>) : ActBasis<AGENT>(creations) {
    override fun create(d: AGENT, n: Int): ActBasis<AGENT> {
        return InteractionBasis(d1,d2,creations.plus(d,n))
    }

    override fun annihilate(d: AGENT): ActBasis<AGENT> {
        throw(NotImplementedError())
    }

    override fun multiplyTo(otherBasis: ActCreationBasis<AGENT>,
                            ground: GroundState<AGENT>,
                            termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit) {
        val lambda1 = ground.lambda(d1)
        val lambda2 = ground.lambda(d2)
        val n1 = otherBasis.creations[d1]?.toDouble()?:0.0
        val n2 = otherBasis.creations[d2]?.toDouble()?:0.0
        // hoping these will get translated to AVX vector instructions!
        val cll = lambda1*lambda2
        val cnl = n1*lambda2
        val cln = lambda1*n2
        val cnn = n1*n2
        var cminusd12 : Map<AGENT,Int>? = null
        val creationUnion = this.creations union otherBasis.creations
        if(cll != 0.0) termConsumer(ActCreationBasis(creationUnion), cll)
        if(cnl != 0.0) {
            val cminusd1 = creationUnion.plus(d1,-1)
            termConsumer(ActCreationBasis(cminusd1),cnl)
            cminusd12 = cminusd1.plus(d2,-1)
        }
        if(cln != 0.0) {
            val cminusd2 = creationUnion.plus(d2,-1)
            termConsumer(ActCreationBasis(cminusd2),cnl)
            if(cminusd12 == null) cminusd12 = cminusd2.plus(d1,-1)
        }
        if(cnn != 0.0) {
            if(cminusd12 == null) cminusd12 = creationUnion.union(d1 to -1, d2 to -1)
            termConsumer(ActCreationBasis(cminusd12),cnl)
        }
    }


    override fun multiplyTo(groundBasis: GroundBasis<AGENT>,
                            termConsumer: (ActCreationBasis<AGENT>, Double) -> Unit) {
        val lambda1 = groundBasis.ground.lambda(d1)
        val lambda2 = groundBasis.ground.lambda(d2)
        val n1 = groundBasis.basis.creations[d1]?.toDouble()?:0.0
        val n2 = groundBasis.basis.creations[d2]?.toDouble()?:0.0
        val cll = lambda1*lambda2
        val cnl = n1*lambda2
        val cln = lambda1*n2
        val cnn = n1*n2
        var cminusd12 : Map<AGENT,Int>? = null
        if(cll != 0.0) termConsumer(ActCreationBasis(creations), cll)
        if(cnl != 0.0) {
            val cminusd1 = creations.plus(d1,-1)
            termConsumer(ActCreationBasis(cminusd1),cnl)
            cminusd12 = cminusd1.plus(d2,-1)
        }
        if(cln != 0.0) {
            val cminusd2 = creations.plus(d2,-1)
            termConsumer(ActCreationBasis(cminusd2),cnl)
            if(cminusd12 == null) cminusd12 = cminusd2.plus(d1,-1)
        }
        if(cnn != 0.0) {
            if(cminusd12 == null) cminusd12 = creations.union(d1 to -1, d2 to -1)
            termConsumer(ActCreationBasis(cminusd12),cnl)
        }
    }


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