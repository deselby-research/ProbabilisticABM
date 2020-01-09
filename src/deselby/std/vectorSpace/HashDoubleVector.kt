package deselby.std.vectorSpace

import java.util.function.BiFunction
import kotlin.math.abs
import kotlin.math.absoluteValue

class HashDoubleVector<BASIS>(val coeffs : HashMap<BASIS,Double>) : MutableDoubleVector<BASIS>, AbstractMutableMap<BASIS,Double>() {
    override val entries
            get() = coeffs.entries

    constructor() : this(HashMap())

    constructor(vecToCopy : Vector<BASIS,Double>) : this(HashMap(vecToCopy))

    constructor(vararg mappings : Pair<BASIS,Double>) : this(HashMap(mappings.size)) {
        coeffs.putAll(mappings)
    }

    override fun put(key: BASIS, value: Double) = coeffs.put(key,value)

    override fun get(key: BASIS) = coeffs.get(key)

    override fun remove(key: BASIS) = coeffs.remove(key)

    override fun remove(key: BASIS, value: Double) = coeffs.remove(key, value)

    override fun merge(key: BASIS, value: Double, remappingFunction: BiFunction<in Double, in Double, out Double?>): Double?
        = coeffs.merge(key, value, remappingFunction)


    override fun toMutableVector() = HashDoubleVector(HashMap(coeffs))

    override fun zero() = HashDoubleVector<BASIS>(HashMap())

    override fun toString() : String {
        if(coeffs.isEmpty()) return "{ }"
        val sortedTerms = entries.sortedByDescending { abs(it.value) }
        return buildString {
            var firstTerm = true
            sortedTerms.forEach {
                if(it.value > 0.0) {
                    if(!firstTerm) append(" + ")
                } else {
                    append(" - ")
                }
                firstTerm = false
                if(it.value.absoluteValue == 1.0) {
                    val basis = it.key.toString()
                    if(basis.isNotEmpty()) append(it.key.toString()) else append("1.0")
                } else {
                    append("%f%s ".format(it.value.absoluteValue, it.key))
                }
            }
        }
//        var s = ""
//        s.as
//        coeffs.forEach {
//            s += "%+fP[%s] ".format(it.value, it.key)// ""${it.coeff}P[${it.key}] "
//        }
//        return s
    }
}