package deselby.std.abstractAlgebra

interface HasTimesBySelf<T : HasTimesBySelf<T>> {
    operator fun times(other: T): T
}
