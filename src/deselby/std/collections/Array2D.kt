package deselby.std.collections

typealias Array2D<T> = Array<Array<T>>

inline fun <reified T> Array2D(xSize : Int, ySize : Int, crossinline initialiser : (Int, Int) -> T) : Array<Array<T>> {
    return Array(xSize) { x ->
        Array(ySize) { y ->
            initialiser(x, y)
        }
    }
}

val <T> Array2D<T>.iSize : Int
    get()= size

val <T> Array2D<T>.jSize : Int
    get() = if(size == 0) 0 else get(0).size

fun <T> Array2D<T>.toString2D() : String {
    var s = String()
    this.forEach {
        it.forEach {
            s += "${it} "
        }
        s +="\n"
    }
    return s
}
