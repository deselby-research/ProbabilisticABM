package deselby.std.abstractAlgebra

interface HasTimesAssign<MULTIPLIER> {
    operator fun timesAssign(multiplier : MULTIPLIER)
}