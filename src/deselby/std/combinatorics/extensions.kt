package deselby.std.combinatorics

inline fun<reified T> Sequence<Sequence<T>>.combinations() : Sequence<List<T>> {
    val size = this.count()
    val sequenceIt = this.iterator()
    val iterators = Array(size) {
        sequenceIt.next().iterator()
    }
    val combination = Array(size) {
        iterators[it].next()
    }
    return generateSequence(CombinationIterator(this, iterators, combination)) {
        it.next()
    }.map {it.combination.asList()}
}


inline fun<reified T> Collection<Sequence<T>>.combinations() : Sequence<List<T>> {
    val sequenceIt = this.iterator()
    val iterators = Array(size) {
        sequenceIt.next().iterator()
    }
    val combination = Array(size) {
        iterators[it].next()
    }
    return generateSequence(CombinationIterator(this.asSequence(), iterators, combination)) {
        it.next()
    }.map {it.combination.asList()}
}
