package deselby.std.vectorSpace

import java.util.Collections.singleton

class OneHotDoubleVector<BASIS>(val basis : BASIS, val coeff : Double) :
        AbstractMap<BASIS,Double>(),
        DoubleVector<BASIS> {

    override val entries: Set<Map.Entry<BASIS, Double>>
        get() = singleton(this.asEntry())


    override fun get(key: BASIS) : Double? {
        return if(key == basis) coeff else null
    }

    override fun toMutableVector(): HashDoubleVector<BASIS> {
        return HashDoubleVector(hashMapOf(basis to coeff))
    }

    override fun zero(): HashDoubleVector<BASIS> {
        return HashDoubleVector()
    }

    fun asEntry() : Map.Entry<BASIS,Double> {
        return object : Map.Entry<BASIS,Double> {
            override val key: BASIS
                get() = basis
            override val value: Double
                get() = coeff
        }
    }

    override operator fun plus(other: Vector<BASIS,Double>): HashDoubleVector<BASIS> {
        val result = HashDoubleVector(other)
        result.coeffs.merge(basis , coeff) { a, b ->
            val newVal = a + b
            if(newVal == 0.0) null else newVal
        }
        return result
    }


    override operator fun minus(other: Vector<BASIS,Double>): HashDoubleVector<BASIS> {
        val result = HashDoubleVector<BASIS>()
        other.mapValuesTo(result.coeffs) { -it.value }
        result += this.asEntry()
        return result
    }


    override operator fun unaryMinus(): OneHotDoubleVector<BASIS> =
            OneHotDoubleVector(basis, -coeff)


    override operator fun times(multiplier: Double): OneHotDoubleVector<BASIS> =
            OneHotDoubleVector(basis, coeff*multiplier)


    override fun toString(): String {
        return "%+fP[%s]".format(coeff, basis)
    }
}
