package deselby.std.vectorSpace

abstract class AbstractMutableDoubleVector<BASIS> : AbstractDoubleVector<BASIS>(), MutableVector<BASIS,Double> {

    override fun plusAssign(other: Vector<BASIS, Double>) {
        other.coeffs.forEach { this += it}
    }


    override fun minusAssign(other: Vector<BASIS, Double>) {
        other.coeffs.forEach { this -= it}
    }


    override fun timesAssign(multiplier: Double) {
        if(multiplier == 0.0) coeffs.clear()
        coeffs.entries.forEach {
            it.setValue(it.value * multiplier)
        }
    }


    operator fun plusAssign(entry : Map.Entry<BASIS,Double>) {
        coeffs.merge(entry.key , entry.value) {a , b ->
            val newVal = a + b
            if(newVal == 0.0) null else newVal
        }
    }


    operator fun minusAssign(entry : Map.Entry<BASIS,Double>) {
        coeffs.merge(entry.key , -entry.value) {a , b ->
            val newVal = a + b
            if(newVal == 0.0) null else newVal
        }
    }
}
