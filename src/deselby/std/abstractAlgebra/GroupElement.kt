package deselby.std.abstractAlgebra

interface GroupElement<ELEMENT : GroupElement<ELEMENT>> {
   // fun isZero() : Boolean
    operator fun plus(other : ELEMENT) : ELEMENT
    operator fun unaryMinus() : ELEMENT
    operator fun minus(other : ELEMENT): ELEMENT = unaryMinus().plus(other)
}