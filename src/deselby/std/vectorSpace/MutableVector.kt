package deselby.std.vectorSpace

import deselby.std.abstractAlgebra.HasMutablePlusMinusSelf
import deselby.std.abstractAlgebra.HasTimesAssign

interface MutableVector<BASIS,SCALAR> :
        MutableMap<BASIS,SCALAR>,
        Vector<BASIS, SCALAR>,
        HasMutablePlusMinusSelf<Vector<BASIS,SCALAR>>,
        HasTimesAssign<SCALAR> {
//    override val coeffs : MutableMap<BASIS,SCALAR>

}

