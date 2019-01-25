package deselby.operators

open class UnaryOperator<T>(val operand : T) : Operator<T> {
}

class Annihilate<T>(state : T) : UnaryOperator<T>(state)
class Create<T>(state : T) : UnaryOperator<T>(state)
class Constant(v : Double) : UnaryOperator<Double>(v)
