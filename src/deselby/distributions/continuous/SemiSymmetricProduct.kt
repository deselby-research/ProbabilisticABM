package deselby.distributions.continuous

// Here we know which dimensions of 'a' are identified with dimensions of 'b'
// by looking at which dimensions are already named in 'b'
class SemiSymmetricProduct(val a : FourierDistribution, val b : SemiSymmetricDistribution) : SemiSymmetricDistribution {

    fun weightedIntegral(weights: FourierDistribution, dimensions : BooleanArray): LiftedSymmetricDistribution {
        // first integrate over any unnamed dimensions

        // now integrate over any named dimensions
        TODO("Implement this")
    }

    override fun lower(nDimensions: Int): SymmetricDistribution {
        throw(IllegalArgumentException("You probably didn't mean to lower a SemiSymmetricProduct!"))
    }

    override fun integrate(dimensions: BooleanArray): LiftedSymmetricDistribution {
        dimensions.forEachIndexed { i, isToIntegrate ->
            if(i < a.shape.size && a.shape[i] > 1) dimensions[i] = false
        }
        val integratedDist = b.integrate(dimensions)
        TODO("To finish")
    }



    override fun nameDimension(name: Int): SemiSymmetricDistribution =
            SemiSymmetricProduct(a, b.nameDimension(name))

//    override fun lower(baseDimension: Int, nDimensions: Int): ContinuousDistribution =
//            SemiSymmetricProduct(a.lower(baseDimension, nDimensions), b.lower(baseDimension, nDimensions))
//
//    override fun lift(baseDimension : Int, nDimensions : Int) : ContinuousDistribution =
//            SemiSymmetricProduct(a.lift(baseDimension, nDimensions), b.lift(baseDimension, nDimensions))



}