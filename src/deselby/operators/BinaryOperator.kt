package deselby.operators

open class BinaryOperator<A,B,T>(val op1 : A, val op2 : B) : Operator<T> {
}

class Integrate<T>(over : (T)->Boolean, op : Operator<T>) : BinaryOperator<(T)->Boolean, Operator<T>,T>(over, op)
class Add<T>(op1 : Operator<T>, op2 : Operator<T>) : BinaryOperator<Operator<T>, Operator<T>,T>(op1, op2)
class Times<T>(op1 : Operator<T>, op2 : Operator<T>) : BinaryOperator<Operator<T>, Operator<T>,T>(op1, op2)
