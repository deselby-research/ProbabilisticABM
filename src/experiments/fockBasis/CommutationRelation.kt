package experiments.fockBasis

// represents the commutation relation [x,H] where x commutes with the creation operator
// and annihilationCommutation is the commutation with the annihilation operator [x,a]
class CommutationRelation<AGENT>(val operatorToCommute : FockState<AGENT>,
                                 val H : FockState<AGENT> = SparseFockDecomposition(),
                                 val commutation: FockState<AGENT> = ZeroFockState()) : Fockable<AGENT> {

    override fun annihilate(d: AGENT): CommutationRelation<AGENT> {
        val a  = Operators.annihilate(d)
        val annihilationCommutation = operatorToCommute*a - a*operatorToCommute
        return CommutationRelation(
                operatorToCommute,
                H.annihilate(d),
                commutation.annihilate(d) + annihilationCommutation*H)
    }

    override fun create(a: AGENT, n: Int): CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute , H.create(a, n), commutation.create(a, n))
    }

    override fun create(d : AGENT) : CommutationRelation<AGENT> = create(d,1)

    operator fun plus(other : CommutationRelation<AGENT>) : CommutationRelation<AGENT> {
        return CommutationRelation(operatorToCommute, H + other.H, commutation + other.commutation)
    }
}