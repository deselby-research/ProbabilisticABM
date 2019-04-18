package deselby.distributions.continuous

// Here we know which dimensions of 'a' are identified with dimensions of 'b'
// by looking at which dimensions are already named in 'b'
class SemiSymmetricProduct(val a : ContinuousDistribution, val b : SemiSymmetricDistribution) : SemiSymmetricDistribution {
    override fun nameDimension(name: Int): SemiSymmetricDistribution =
            SemiSymmetricProduct(a, b.nameDimension(name))

//    override fun lower(baseDimension: Int, nDimensions: Int): ContinuousDistribution =
//            SemiSymmetricProduct(a.lower(baseDimension, nDimensions), b.lower(baseDimension, nDimensions))
//
//    override fun lift(baseDimension : Int, nDimensions : Int) : ContinuousDistribution =
//            SemiSymmetricProduct(a.lift(baseDimension, nDimensions), b.lift(baseDimension, nDimensions))



}