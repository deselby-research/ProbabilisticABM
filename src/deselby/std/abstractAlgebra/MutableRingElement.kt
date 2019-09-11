package deselby.std.abstractAlgebra

interface MutableRingElement<ELEMENT>:
        RingElement<ELEMENT>,
        HasMutablePlusMinusSelf<ELEMENT>,
        HasMutableTimesBySelf<ELEMENT>
        where
ELEMENT: HasPlusMinusSelf<ELEMENT>,
ELEMENT: HasTimesBySelf<ELEMENT>
