package deselby.std.abstractAlgebra

interface HasMutablePlusMinusSelf<T : HasPlusMinusSelf<T>> : HasPlusMinusSelf<T> {
    operator fun plusAssign(other: T)
    operator fun minusAssign(other: T)
}