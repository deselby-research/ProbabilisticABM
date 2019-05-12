package deselby.std

class DoubleProgression(override val start : Double, override val endInclusive : Double, val step : Double) : ClosedFloatingPointRange<Double>, Iterable<Double> {
    override fun lessThanOrEquals(a: Double, b: Double) = a <= b
    override fun iterator() = DoubleProgressionIterator(start, endInclusive, step)
}

class DoubleProgressionIterator(var value : Double, val endInclusive : Double, val step : Double) : Iterator<Double> {
    override fun hasNext(): Boolean {
        return if(step>0) endInclusive >= value else endInclusive <= value
    }

    override fun next(): Double {
        val x = value
        value += step
        return x
    }
}

infix fun <T> ClosedFloatingPointRange<T>.step(step : Double) : DoubleProgression where T : Number, T : Comparable<T> {
    return DoubleProgression(this.start.toDouble(), this.endInclusive.toDouble(), step)
}