package deselby.std.abstractAlgebra

import javax.swing.text.AbstractDocument

interface MutableAlgebraElement<ELEMENT, SCALAR> :
        AlgebraElement<ELEMENT, SCALAR>,
        HasMutableTimesBySelf<ELEMENT>,
        HasMutablePlusMinusSelf<ELEMENT>,
        HasTimesAssign<SCALAR>
        where
ELEMENT : HasPlusMinusSelf<ELEMENT>,
ELEMENT : HasTimesBySelf<ELEMENT> {}