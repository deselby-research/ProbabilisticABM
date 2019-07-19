package experiments.fockBasis

// represents the commutation relation [x,H] where x commutes with the creation operator
// and annihilationCommutation is the commutation with the annihilation operator [x,a]
class CommutationRelation<AGENT>
    : FockState<AGENT,CommutationRelation<AGENT>> {

    val operatorToCommute: OperatorBasis<AGENT>
    val H: AbstractFockState<AGENT>
    val commutation: AbstractFockState<AGENT>

    constructor(operatorToCommute: OperatorBasis<AGENT>) {
        this.operatorToCommute = operatorToCommute
        this.H = SparseFockDecomposition()
        commutation = ZeroFockState()
    }

    private constructor(operatorToCommute: OperatorBasis<AGENT>, H: AbstractFockState<AGENT>, commutation: AbstractFockState<AGENT>) {
        this.operatorToCommute = operatorToCommute
        this.H = H
        this.commutation = commutation
    }

    override fun times(multiplier: Double): CommutationRelation<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun times(other: CommutationRelation<AGENT>): CommutationRelation<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unaryMinus(): CommutationRelation<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun annihilate(d: AGENT): CommutationRelation<AGENT> {
        val a  = Operators.annihilate(d)
        val x = OneHotFock(operatorToCommute,1.0)
        val annihilationCommutation = x*a - a*x
        return CommutationRelation(
                operatorToCommute,
                H.annihilate(d),
                commutation.annihilate(d) + annihilationCommutation*H)
    }

    override fun create(a: AGENT, n: Int): CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute , H.create(a, n), commutation.create(a, n))
    }

    override fun create(d : AGENT) : CommutationRelation<AGENT> = create(d,1)

    override operator fun plus(other : CommutationRelation<AGENT>) : CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute, H + other.H, commutation + other.commutation)
    }
}