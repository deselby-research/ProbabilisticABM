package deselby.std

class LambdaList<T>(override val size: Int, val generator : (Int) -> T) : AbstractList<T>() {
    override fun get(index: Int) = generator(index)
}

