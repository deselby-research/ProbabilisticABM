package deselby.std.combinatorics

// Iterates over a sequence of sequences to create a sequence of combinations of elements, one from each inner sequence
// Don't use this directly, just create a sequence of sequences and use the .combinations() extension
class CombinationIterator<T>(val sequences : Sequence<Sequence<T>>, val iterators : Array<Iterator<T>>, val combination : Array<T>) {
    fun next() : CombinationIterator<T>? {
        var i = -1
        val sequence = sequences.iterator()
        while(!iterators[++i].hasNext()) {
            if(i == combination.size-1) return null
            iterators[i] = sequence.next().iterator()
            combination[i] = iterators[i].next()
        }
        combination[i] = iterators[i].next()
        return this
    }
}
