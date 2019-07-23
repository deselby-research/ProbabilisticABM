package deselby.std.abstractAlgebra

interface MutableAdditionGroupElement<MEMBER : AdditionGroupElement<MEMBER>> : AdditionGroupElement<MEMBER> {
    operator fun plusAssign(other : MEMBER)
    operator fun minusAssign(other : MEMBER)
    fun setToZero()
}
