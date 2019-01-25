package deselby

class Behaviour<T>(val rate : Double, val selector : (T) -> Boolean, val op : (T) -> T) {
}