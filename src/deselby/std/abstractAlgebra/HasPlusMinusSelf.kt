package deselby.std.abstractAlgebra

interface HasPlusMinusSelf<T : HasPlusMinusSelf<T>> {
    operator fun plus(other: T): T
    operator fun minus(other: T): T
    operator fun unaryMinus() : T
}