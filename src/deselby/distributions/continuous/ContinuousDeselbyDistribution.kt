package deselby.distributions.continuous

class ContinuousDeselbyDistribution(val rho : FourierDistribution, val subsets : MutableList<SymmetricDistribution>) {

    constructor(rho : FourierDistribution, nSubsets : Int, creator : (Int) -> SymmetricDistribution) :
            this(rho, ArrayList(nSubsets)) {
        for(i in 0 until nSubsets) {
            subsets.add(creator(i))
        }
    }

    // Add nDimensions dimensions to the shape of each of the members of this
    // ready for parametric creation and annihilation creationVector
    fun lift(nDimensions : Int) : LiftedDeselbyDistribution {
        return LiftedDeselbyDistribution(rho, subsets.size) { i ->
            subsets[i].toLiftedSymmetric(nDimensions)
        }
    }

    operator fun plus(dist : SymmetricDistribution) : ContinuousDeselbyDistribution {
        return if (dist.degree() >= subsets.size) ContinuousDeselbyDistribution(rho, dist.degree()+1) { i ->
            when {
                i < subsets.size -> subsets[i]
                i == dist.degree() -> dist
                else -> SymmetricDistribution(i)
            }
        }
        else ContinuousDeselbyDistribution(rho, subsets.size) { i ->
            if(i != dist.shape.size) subsets[i] else subsets[i] + dist
        }
    }

    operator fun plusAssign(dist : SymmetricDistribution) {
        while(dist.degree() >= subsets.size) {
            subsets.add(SymmetricDistribution(subsets.size))
        }
        subsets[dist.degree()] += dist
    }
}