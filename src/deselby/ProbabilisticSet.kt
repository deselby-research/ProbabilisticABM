package deselby

import deselby.operators.Annihilate
import deselby.operators.Create
import deselby.operators.Operator

class ProbabilisticSet<T> {
    fun create(member : T) : Operator<T> {
        return Create(member)
    }

    fun delete(member : T) : Operator<T> {
        return Annihilate(member)
    }


}