package deselby.std.abstractAlgebra

interface AlgebraElement<ELEMENT, SCALAR> : RingElement<ELEMENT>, HasTimes<SCALAR,ELEMENT>
    where ELEMENT : HasPlusMinusSelf<ELEMENT>,
          ELEMENT : HasTimesBySelf<ELEMENT>
{
}
