package deselby.operators

interface Operator<T> {
    operator fun plus(other : Operator<T>) : Operator<T> {
        return Add( this, other)
    }

    operator fun times(other : Operator<T>) : Operator<T> {
        return Times( this, other)
    }
}