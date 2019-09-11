package deselby.std.vectorSpace

import deselby.std.abstractAlgebra.HasTimes

interface CovariantVector<out BASIS, SCALAR> {
//    val size: Int
    val entries: Set<Map.Entry<BASIS, SCALAR>>
//    val keys: Set<BASIS>
//    val values: Collection<SCALAR>
//    fun isEmpty(): Boolean
}
