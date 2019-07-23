package deselby.std.abstractAlgebra

interface MultiplicationMonoidElement<ELEMENT : MultiplicationMonoidElement<ELEMENT>> {
    // fun isOne() : Boolean
    operator fun times(other : ELEMENT) : ELEMENT
}