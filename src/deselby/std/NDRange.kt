package deselby.std

import kotlin.math.max
import kotlin.math.min

class NDRange(val range : Array<IntProgression>) : Iterable<IntArray> {

    val nDimensions : Int
        get() = range.size

    val shape : IntArray
        get() = IntArray(nDimensions) {
        (this[it].last - this[it].first + 1)/this[it].step
    }

    constructor(size : Int, filler : (Int) -> IntProgression) : this(Array(size, filler))

    operator fun get(dimension : Int) = range.get(dimension)

    operator fun set(dimension : Int, intRange : IntProgression) {
        range[dimension] = intRange
    }

    override fun iterator(): Iterator<IntArray> {
        return NDRangeIterator(range)
    }

    infix fun step(step : IntArray) = NDRange(Array(range.size) {
        IntProgression.fromClosedRange(range[it].first, range[it].last, step[it])
    })

    fun first() = IntArray(range.size) {
        range[it].first
    }

    fun last() = IntArray(range.size) {
        range[it].last
    }


    // smallest bounding box (with step 1) that contains both NDRanges
    fun rectangularUnion(other : NDRange) : NDRange {
        if(nDimensions != other.nDimensions)
            throw(IllegalArgumentException("Union of NDRanges with differing dimensionality: This is probably not what you intended to do"))
        return NDRange(nDimensions) {
            min(range[it].first, other.range[it].first) .. max(range[it].last, other.range[it].last)
        }
    }

    // largest box (ignoring steps) that is contained within both NDRanges
    fun rectangularIntersection(other : NDRange) : NDRange {
        if(nDimensions != other.nDimensions)
            throw(IllegalArgumentException("Intersection of NDIndexSets with differing dimensionality: This is probably not what you intended to do"))
        return NDRange(nDimensions) {
            max(range[it].first, other.range[it].first) .. min(range[it].last, other.range[it].last)
        }
    }
}

class NDRangeIterator(val range : Array<out IntProgression>) : Iterator<IntArray> {
    val index = IntArray(range.size) { if(it == range.size-1) range[it].first-range[it].step else range[it].first }

    override fun hasNext(): Boolean {
        return index.foldIndexed(false) {i, acc, v -> acc || v < range[i].last}
    }

    override fun next(): IntArray {
        var i = index.size - 1
        index[i] += range[i].step
        while (index[i] > range[i].last) {
            index[i] = range[i].first
            index[--i] += range[i].step
        }
        return index
    }
}

operator fun IntArray.rangeTo(other : IntArray) = NDRange(Array(this.size) {
    this[it] .. other[it]
})

infix fun IntArray.until(other : IntArray) = NDRange(Array(this.size) {
    this[it] until other[it]
})