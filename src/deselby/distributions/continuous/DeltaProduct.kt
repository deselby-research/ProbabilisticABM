package deselby.distributions.continuous

class DeltaProduct(val parameterId : Int, val dist : SemiSymmetricDistribution) : SemiSymmetricDistribution {
    override fun integrate(dimensions: BooleanArray): LiftedSymmetricDistribution {
        if(!dimensions[parameterId]) {
            return dist.integrate(dimensions)
        }
        dimensions[parameterId] = false
        val integratedDist = dist.integrate(dimensions)

    }

    override fun lower(nDimensions: Int): SymmetricDistribution {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nameDimension(name: Int): SemiSymmetricDistribution =
            DeltaProduct(parameterId, dist.nameDimension(name))



}