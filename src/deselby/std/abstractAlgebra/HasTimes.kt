package deselby.std.abstractAlgebra

interface HasTimes<in MULTIPLIER, out RESULT> {
    operator fun times(multiplier: MULTIPLIER) : RESULT
}
