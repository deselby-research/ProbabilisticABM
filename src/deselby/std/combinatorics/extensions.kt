package deselby.std.combinatorics

import org.apache.commons.math3.util.CombinatoricsUtils

//inline fun<reified T> Sequence<Sequence<T>>.combinations() : Sequence<List<T>> {
//    val size = this.count()
//    val sequenceIt = this.iterator()
//    val iterators = Array(size) {
//        sequenceIt.next().iterator()
//    }
//    val combination = Array(size) {
//        iterators[it].next()
//    }
//    return object : Sequence<List<T>> {
//        override fun iterator() = CombinationIterator<T>(this@combinations, iterators, combination)
//    }
//}


inline fun<reified T> Collection<Iterable<T>>.combinations() : Sequence<List<T>> {
    val thisIt = this.iterator()
    val iterators = Array(size) {
        thisIt.next().iterator()
    }

    val firstCombination = Array(size) {
        iterators[it].next()
    }

    return object : Sequence<List<T>> {
        override fun iterator() = CombinationIterator(this@combinations, iterators, firstCombination)
    }
}

fun Int.choose(m: Int): Long {
    return CombinatoricsUtils.binomialCoefficient(this, m)
}
