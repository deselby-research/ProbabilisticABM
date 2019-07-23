package deselby.std.abstractAlgebra

interface RingElement<ELEMENT> : AdditionGroupElement<ELEMENT>, MultiplicationMonoidElement<ELEMENT>
        where   ELEMENT: AdditionGroupElement<ELEMENT>,
                ELEMENT: MultiplicationMonoidElement<ELEMENT>
