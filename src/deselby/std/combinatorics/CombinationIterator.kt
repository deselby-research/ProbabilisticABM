package deselby.std.combinatorics

// Iterates over a sequence of sequences to create a sequence of combinations of elements, one from each inner sequence
// Don't use this directly, just create a sequence of sequences and use the .combinations() extension
class CombinationIterator<T>(val sequences : Iterable<Iterable<T>>, val iterators : Array<Iterator<T>>, val nextCombination : Array<T>) : Iterator<List<T>> {
    var nextCombinationIsStale = false

    override fun hasNext(): Boolean {
        if(nextCombinationIsStale) {
            nextCombinationIsStale = false
            var i = -1
            val sequence = sequences.iterator()
            while (!iterators[++i].hasNext()) {
                if (i == nextCombination.size - 1) return false
                iterators[i] = sequence.next().iterator()
                nextCombination[i] = iterators[i].next()
            }
            nextCombination[i] = iterators[i].next()
        }
        return true
    }

    override fun next(): List<T> {
        if(nextCombinationIsStale) hasNext()
        nextCombinationIsStale = true
        return nextCombination.toList()
    }
}
