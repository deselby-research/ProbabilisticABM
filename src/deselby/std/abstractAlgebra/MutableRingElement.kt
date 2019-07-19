package deselby.std.abstractAlgebra

interface MutableRingElement<MEMBER : RingElement<MEMBER>> : RingElement<MEMBER>, MutableGroupElement<MEMBER> {
    operator fun timesAssign(other : MEMBER)
    // fun setToOne()
}