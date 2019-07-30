package deselby.std.vectorSpace

import deselby.std.abstractAlgebra.HasPlusMinusSelf
import deselby.std.abstractAlgebra.HasTimes

interface Vector<BASIS, SCALAR> :
        HasPlusMinusSelf<Vector<BASIS,SCALAR>>,
        HasTimes<SCALAR, Vector<BASIS,SCALAR>> {
    val coeffs : Map<BASIS,SCALAR>

//    fun toMutableVector() : MutableVector<BASIS,SCALAR>
//    fun zero() : MutableVector<BASIS,SCALAR>

}