package deselby.std.abstractAlgebra

interface MutableMultiplicationMonoidElement<ELEMENT : MultiplicationMonoidElement<ELEMENT>> : MultiplicationMonoidElement<ELEMENT> {
    operator fun timesAssign(other : ELEMENT)
    // fun setToOne()
}