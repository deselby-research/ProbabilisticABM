package experiments.fockBasis

// represents the commutation relation [x,H] where x commutes with the creation operator
// and annihilationCommutation is the commutation with the annihilation operator [x,a]
class CommutationRelation<AGENT>
    : FockState<AGENT,CommutationRelation<AGENT>> {

    val operatorToCommute: OperatorBasis<AGENT>
    val H: MapFockState<AGENT>
    val commutation: MapFockState<AGENT>

    constructor(operatorToCommute: OperatorBasis<AGENT>) {
        this.operatorToCommute = operatorToCommute
        this.H = OperatorBasis.identity<AGENT>().toFockState()
        commutation = ZeroFockState()
    }

    private constructor(operatorToCommute: OperatorBasis<AGENT>, H: MapFockState<AGENT>, commutation: MapFockState<AGENT>) {
        this.operatorToCommute = operatorToCommute
        this.H = H
        this.commutation = commutation
    }

    override fun create(creations: Map<AGENT, Int>): CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute, H.create(creations), commutation.create(creations))
    }

    override fun times(multiplier: Double): CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute, H * multiplier, commutation * multiplier)
    }

    override fun times(other: CommutationRelation<AGENT>): CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute,
                H * other.H,
                commutation*other.H + H*other.commutation)
    }

//    override fun unaryMinus(): CommutationRelation<AGENT> {
//        return CommutationRelation(operatorToCommute, H.unaryMinus(), commutation.unaryMinus())
//    }

    override fun annihilate(d: AGENT): CommutationRelation<AGENT> {
        val a  = OperatorBasis.annihilate(d)
        val x = operatorToCommute
        return CommutationRelation(
                operatorToCommute,
                H.annihilate(d),
                commutation.annihilate(d) + (x*a - a*x)*H)
    }

    override fun create(a: AGENT, n: Int): CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute , H.create(a, n), commutation.create(a, n))
    }

    override fun create(d : AGENT) : CommutationRelation<AGENT> = create(d,1)

    override operator fun plus(other : CommutationRelation<AGENT>) : CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute, H + other.H, commutation + other.commutation)
    }

    override fun toString(): String {
        return commutation.toString()
    }
}