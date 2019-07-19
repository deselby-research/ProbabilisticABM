package deselby.std.abstractAlgebra

interface MutableGroupElement<MEMBER : GroupElement<MEMBER>> : GroupElement<MEMBER> {
    operator fun plusAssign(other : MEMBER)
    operator fun minusAssign(other : MEMBER)
    fun setToZero()
}
