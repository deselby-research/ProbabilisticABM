package deselby.distributions.discrete

import deselby.distributions.FockState
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import java.util.HashMap
import kotlin.math.ln

class GeneratorPolynomial<ABM: MutableCollection<AGENT>, AGENT> private constructor(val coeffs: ArrayList<WeightedState<ABM>>): FockState<AGENT, GeneratorPolynomial<ABM, AGENT>> {


    class WeightedState<ABMTYPE>(var abmState : ABMTYPE, var probability : Double)

    val size: Int
        get() = coeffs.size

    constructor(initialState: ABM): this(arrayListOf(WeightedState<ABM>(initialState, 1.0)))

    override fun create(d: AGENT): GeneratorPolynomial<ABM, AGENT> {
        coeffs.forEach { wState ->
            wState.abmState.add(d)
        }
        return this
    }


    // AGENT should define the 'equals' method to ensure we can
    // compare states
    override fun annihilate(d: AGENT): GeneratorPolynomial<ABM, AGENT> {
        coeffs.forEach { wState ->
            wState.probability *= wState.abmState.count {it == d}
            wState.abmState.remove(d)
        }
        coeffs.removeIf { wState ->
            wState.probability == 0.0
        }
        return this
    }

    override fun number(d: AGENT): GeneratorPolynomial<ABM,AGENT> {
        coeffs.forEach { wState ->
            wState.probability *= wState.abmState.count {it == d}
        }
        return this
    }



    override operator fun plus(other : GeneratorPolynomial<ABM, AGENT>) : GeneratorPolynomial<ABM, AGENT> {
        val fuzzyUnion = ArrayList<WeightedState<ABM>>(size + other.size)
        fuzzyUnion.addAll(coeffs)
        fuzzyUnion.addAll(other.coeffs)
        return GeneratorPolynomial(fuzzyUnion)
    }

    override fun minus(other: GeneratorPolynomial<ABM, AGENT>): GeneratorPolynomial<ABM, AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun times(const: Double): GeneratorPolynomial<ABM, AGENT> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


//    // randomly chooses a single monomial from this polynomial with probability proportional to
//    // its coefficient and returns a GeneratorPolynomial<AGENT> with just that monomial in, with
//    // coefficient of 1
//    // assumes that this polynomial is normalised
//    fun sample(rand : RandomGenerator = MersenneTwister()) : GeneratorPolynomial<AGENT> {
//        val targetCumulativeProb = rand.nextDouble()
//        var cumulativeProb = 0.0
//        val iterator = coeffs.iterator()
//        var entry : MutableMap.MutableEntry<List<AGENT>,Double>? = null
//        while(cumulativeProb <= targetCumulativeProb && iterator.hasNext()) {
//            entry = iterator.next()
//            cumulativeProb += entry.value
//        }
//        val result = GeneratorPolynomial<AGENT>(HashMap())
//        if(entry != null) result[entry.key] = 1.0
//        return result
//    }
//
//    // returns a (dt, monomial) pair where dt is the time elapsed, and monomial is the state that is transitioned
//    // to from this.
//    fun sampleNext(hamiltonian : (FockState<Int, GeneratorPolynomial<AGENT>>) -> GeneratorPolynomial<AGENT>, rand : RandomGenerator = MersenneTwister()) : Pair<Double, GeneratorPolynomial<AGENT>> {
//        val p0 = if(this.size == 1) this else this.sample()
//        val SparseH = hamiltonian(p0)
//        if(SparseH.size == 0) return Pair(Double.POSITIVE_INFINITY, this)
//        val currentState = p0.coeffs.keys.first()
//        val totalRate = -(SparseH[currentState]?:throw(IllegalArgumentException()))
//        SparseH.coeffs.remove(currentState)
//        return Pair(
//                -ln(1.0 - rand.nextDouble()) /totalRate,
//                (SparseH*(1.0/totalRate)).sample(rand)
//        )
//    }
//
//
//    override fun toString() : String {
//        var s = ""
//        var printPlus = false
//        coeffs.forEach { occupation, p ->
//            if (printPlus) {
//                if(p > 0.0) s += " + " else s += " - "
//            } else {
//                if(p < 0.0) s += "-"
//                printPlus = true
//            }
//            if (p.absoluteValue != 1.0) s += p.absoluteValue.toString()
//            s += "P${occupation}"
//        }
//        return s
//    }

}
