package deselby.std.abstractAlgebra

interface MutableAlgebraElement<MEMBER : AlgebraElement<MEMBER,SCALAR>, SCALAR> :
        AlgebraElement<MEMBER,SCALAR>,
        MutableRingElement<MEMBER>
{
    operator fun timesAssign(other : SCALAR)
}