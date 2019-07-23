package deselby.std.abstractAlgebra

interface MutableRingElement<ELEMENT> : MutableAdditionGroupElement<ELEMENT>, MutableMultiplicationMonoidElement<ELEMENT>
        where ELEMENT : MultiplicationMonoidElement<ELEMENT>,
              ELEMENT : AdditionGroupElement<ELEMENT>
