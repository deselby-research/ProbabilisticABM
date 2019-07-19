package deselby.std.abstractAlgebra

interface RingElement<MEMBER : RingElement<MEMBER>> : GroupElement<MEMBER> {
   // fun isOne() : Boolean
    operator fun times(other : MEMBER) : MEMBER
}