package deselby.std

class LambdaList<T>(override val size : Int, val generator : (Int) -> T) : List<T> {

    inner class LIterator(var index : Int) : ListIterator<T> {
        override fun hasNext(): Boolean     = (index < size-1)
        override fun hasPrevious(): Boolean = index > 0
        override fun next(): T              = generator(++index)
        override fun nextIndex(): Int       = index + 1
        override fun previous(): T          = generator(--index)
        override fun previousIndex(): Int   = index - 1
    }

    override fun contains(element: T): Boolean {
        for(i in 0 until size) {
            if(generator(i) == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        for(e in elements) {
            if(!contains(e)) return false
        }
        return true
    }

    override fun get(index: Int): T {
        if(index >= size) throw(ArrayIndexOutOfBoundsException())
        return(generator(index))
    }

    override fun indexOf(element: T): Int {
        for(i in 0 until size) {
            if(generator(i) == element) return i
        }
        return -1
    }

    override fun isEmpty(): Boolean = (size == 0)

    override fun lastIndexOf(element: T): Int = size-1

    override fun iterator(): Iterator<T> = LIterator(-1)

    override fun listIterator(): ListIterator<T> = LIterator(-1)

    override fun listIterator(index: Int): ListIterator<T> = LIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<T> = LambdaList(toIndex-fromIndex, {i ->
        generator(i + fromIndex)
    })
}