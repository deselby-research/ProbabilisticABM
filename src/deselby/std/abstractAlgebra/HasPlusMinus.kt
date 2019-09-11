package deselby.std.abstractAlgebra

interface HasPlusMinus<T,RESULT> {
    operator fun plus(other : T) : RESULT
    operator fun minus(other : T) : RESULT
    operator fun unaryMinus() : RESULT
}