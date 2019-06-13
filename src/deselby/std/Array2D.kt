package deselby.std

open class Array2D<T>(val grid : Array<Array<T>>) {
    val iSize : Int
        get() = grid.size
    val jSize : Int
        get() = if(iSize == 0) 0 else grid[0].size

    operator fun get(i : Int) = grid[i]
    operator fun iterator() = grid.iterator()

    companion object {
        // need to do this as Kotlin doesn't allow reified constructors!
        inline operator fun <reified T> invoke(xSize : Int, ySize : Int, initialiser : (Int, Int) -> T) : Array2D<T> {
            return Array2D(Array(xSize) { x ->
                Array(ySize) { y ->
                    initialiser(x, y)
                }
            })
        }
    }
}
