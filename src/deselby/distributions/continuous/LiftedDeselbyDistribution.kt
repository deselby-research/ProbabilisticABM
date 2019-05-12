package deselby.distributions.continuous

class LiftedDeselbyDistribution(val rho : FourierDistribution, val subsets : List<SemiSymmetricDistribution>) {

    constructor(rho: FourierDistribution, size: Int, creator: (Int) -> SemiSymmetricDistribution) :
            this(rho, Array<SemiSymmetricDistribution>(size) { i ->
                creator(i)
            }.asList())


    fun annihilate(parameterId: Int) : LiftedDeselbyDistribution {
        val liftedRho = rho.lift(0, parameterId)
        val resultTerms = ArrayList<SemiSymmetricDistribution>(subsets.size * 2)
        for (dist in subsets) {
            resultTerms.add(SemiSymmetricProduct(liftedRho, dist))
            resultTerms.add(dist.nameDimension(parameterId))
        }
        return LiftedDeselbyDistribution(rho, resultTerms)
    }

    fun create(parameterId: Int) : LiftedDeselbyDistribution {
        return LiftedDeselbyDistribution(rho, subsets.size) {i ->
            DeltaProduct(parameterId, subsets[i])
        }
    }

    fun integrate(nDimensions : Int) : ContinuousDeselbyDistribution {
        val result = ContinuousDeselbyDistribution(rho, ArrayList(3))
        for(dist in subsets) {
            result += dist.integrate(BooleanArray(nDimensions){true}).lower(nDimensions)
        }
        return result
    }
}

