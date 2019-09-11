package deselby.std.vectorSpace

import deselby.std.abstractAlgebra.HasMutablePlusMinusSelf
import deselby.std.abstractAlgebra.HasTimesAssign

interface MutableVector<BASIS,SCALAR> :
        MutableMap<BASIS,SCALAR>,
        Vector<BASIS, SCALAR>,
        HasMutablePlusMinusSelf<Vector<BASIS,SCALAR>>,
        HasTimesAssign<SCALAR> {
//    override val coeffs : MutableMap<BASIS,SCALAR>

//    override fun contains(element: Map.Entry<BASIS, SCALAR>): Boolean {
//        return this.entries.contains(element)
//    }
//
//    override fun containsAll(elements: Collection<Map.Entry<BASIS, SCALAR>>): Boolean {
//        return this.entries.containsAll(elements)
//    }
//
//    override fun iterator(): Iterator<Map.Entry<BASIS, SCALAR>> {
//        return this.entries.iterator()
//    }

}

