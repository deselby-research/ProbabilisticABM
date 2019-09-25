package deselby.std.vectorSpace

import kotlin.math.absoluteValue

interface MutableDoubleVector<BASIS> : MutableVector<BASIS,Double>, DoubleVector<BASIS> {
//    val coeffs : MutableMap<BASIS, Double>

    override fun plusAssign(other: Vector<BASIS, Double>) {
        other.forEach { this += it}
    }


    override fun minusAssign(other: Vector<BASIS, Double>) {
        other.forEach { this -= it}
    }


    override fun timesAssign(multiplier: Double) {
        if(multiplier == 0.0) clear()
        entries.forEach {
            it.setValue(it.value * multiplier)
        }
    }


    // value of entry should be non-zero!
    operator fun plusAssign(entry : Map.Entry<BASIS,Double>) {
        merge(entry.key , entry.value) {a , b ->
            val newVal = a + b
            if(newVal == 0.0) null else newVal
        }
    }

    fun plusAssign(basis : BASIS, increment : Double) {
        if(increment == 0.0) return
        merge(basis , increment) {a , b ->
            val newVal = a + b
            if(newVal == 0.0) null else newVal
        }
    }

    fun plusAssign(basis : BASIS, increment : Double, smallestNotZero: Double) {
        if(increment == 0.0) return
        merge(basis , increment) {a , b ->
            val newVal = a + b
            if(newVal.absoluteValue < smallestNotZero) null else newVal
        }
    }


    // value of entry should be non-zero!
    operator fun minusAssign(entry : Map.Entry<BASIS,Double>) {
        merge(entry.key , -entry.value) {a , b ->
            val newVal = a + b
            if(newVal == 0.0) null else newVal
        }
    }

    fun truncateBelow(smallestCoefficient: Double) {
        entries.removeAll { it.value.absoluteValue < smallestCoefficient }
    }
}