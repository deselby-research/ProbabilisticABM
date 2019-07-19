package deselby.std.abstractAlgebra

interface AlgebraElement<MEMBER : AlgebraElement<MEMBER, SCALAR>, SCALAR> : RingElement<MEMBER> {
    operator fun times(multiplier : SCALAR) : MEMBER
}
