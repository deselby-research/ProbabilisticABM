package deselby.std.abstractAlgebra

interface HasPlusMinusAssign<T> {
    operator fun plusAssign(other : T)
    operator fun minusAssign(other : T)
}