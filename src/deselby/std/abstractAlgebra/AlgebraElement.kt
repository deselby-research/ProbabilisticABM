package deselby.std.abstractAlgebra

interface AlgebraElement<ELEMENT : AlgebraElement<ELEMENT,SCALAR>, SCALAR> : RingElement<ELEMENT> {
    operator fun times(multiplier : SCALAR) : ELEMENT
}
