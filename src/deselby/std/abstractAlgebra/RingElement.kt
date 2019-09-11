package deselby.std.abstractAlgebra

interface RingElement<ELEMENT> : HasPlusMinusSelf<ELEMENT>, HasTimesBySelf<ELEMENT>
        where   ELEMENT: HasPlusMinusSelf<ELEMENT>,
                ELEMENT: HasTimesBySelf<ELEMENT>
