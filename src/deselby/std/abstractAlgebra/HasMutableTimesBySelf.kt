package deselby.std.abstractAlgebra

interface HasMutableTimesBySelf<T : HasTimesBySelf<T>> : HasTimesBySelf<T> {
    operator fun timesAssign(other: T)
}