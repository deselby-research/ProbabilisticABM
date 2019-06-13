package deselby.distributions.continuous

interface ContinuousDistribution {
    fun lift(baseDimension : Int, nDimensions : Int) : ContinuousDistribution
    fun lower(baseDimension : Int, nDimensions : Int) : ContinuousDistribution
}
