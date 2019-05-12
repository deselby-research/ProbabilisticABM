package deselby.distributions.continuous

interface SemiSymmetricDistribution {
    fun nameDimension(name : Int) : SemiSymmetricDistribution
    fun integrate(dimensions : BooleanArray) : LiftedSymmetricDistribution
//    fun weightedIntegral(weights : FourierDistribution, dimensions : BooleanArray) : LiftedSymmetricDistribution
    fun lower(nDimensions : Int) : SymmetricDistribution
}