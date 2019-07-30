package deselby.fockSpace

// Represents the likelihood of occupation number, n, given a coeff
// of detection, p, and an observed number, m.
//
// P(n|p,m) \propto n!/((n-m)!m!) p^m(1-p)^{n-m}
//
// Only implementing pre-multiplication with Deselby for now
class BinomialLikelihoodBasis<AGENT>(val p : Map<AGENT,Double>, val m : Map<AGENT,Int>) : FockBasis<AGENT> {
    override fun annihilate(d: AGENT): MapFockState<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun create(d: AGENT, n: Int): FockBasis<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun create(newCreations: Map<AGENT, Int>): FockBasis<AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun count(d: AGENT): Int {
        return 0
    }

    // Implementation of identity
    // B_{p,m}(k) * D_{d,l}(k) = (1-p)^d \sum_q c_qD_{d+m-q,(1-r)l}
    // where
    // c_0 = l^m
    // c_{q+1} = (m-q)(d-q)/((q+1)l) c_q
    override fun times(other: FockBasis<AGENT>): MapFockState<AGENT> {
        if(other is DeselbyBasis<AGENT>) {

        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}