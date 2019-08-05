package deselby.fockSpace.extensions

import deselby.fockSpace.FockBasisVector
import deselby.fockSpace.Operator
import deselby.std.vectorSpace.*
import java.util.*


fun<AGENT, BASIS : FockBasisVector<AGENT, BASIS>> DoubleVector<BASIS>.create(d: AGENT): MutableDoubleVector<BASIS> {
    val result = zero()
    mapKeysTo(result) { it.key.create(d) }
    return result
}


fun<AGENT, BASIS : FockBasisVector<AGENT, BASIS>> DoubleVector<BASIS>.create(d: AGENT, n: Int): MutableDoubleVector<BASIS> {
    val result = zero()
    mapKeysTo(result) { it.key.create(d,n) }
    return result
}


fun<AGENT, BASIS : FockBasisVector<AGENT, BASIS>> DoubleVector<BASIS>.create(creations: Map<AGENT,Int>): MutableDoubleVector<BASIS> {
    val result = zero()
    mapKeysTo(result) { it.key.create(creations) }
    return result
}

fun<AGENT, BASIS : FockBasisVector<AGENT, BASIS>> DoubleVector<BASIS>.annihilate(d: AGENT): DoubleVector<BASIS> {
    val result = zero()
    forEach { monomial ->
        monomial.key.annihilate(d).forEach { annihilatedMonomial ->
            val dummy = AbstractMap.SimpleEntry(annihilatedMonomial.key, annihilatedMonomial.value*monomial.value)
            result += dummy
        }
    }
    return result
}


fun <AGENT, BASIS : FockBasisVector<AGENT, BASIS>> DoubleVector<BASIS>.remove(d: AGENT) = create(d,-1)


operator fun<AGENT,BASIS : FockBasisVector<AGENT, BASIS>> BASIS.times(multiplier : Double) : OneHotDoubleVector<BASIS> =
        OneHotDoubleVector(this, multiplier)


operator fun<BASIS : FockBasisVector<*, BASIS>> Double.times(basis : BASIS) : OneHotDoubleVector<BASIS> =
        OneHotDoubleVector(basis, this)


operator fun<BASIS : FockBasisVector<*, BASIS>> Double.times(other : DoubleVector<BASIS>) : DoubleVector<BASIS> =
    other * this



operator fun <AGENT, OTHERBASIS : FockBasisVector<AGENT, OTHERBASIS>>
        DoubleVector<Operator<AGENT>>.times(other : DoubleVector<OTHERBASIS>) : MutableDoubleVector<OTHERBASIS> {
    val result = other.zero()
    forEach {thisTerm ->
        other.forEach { otherTerm ->
            val basisProduct = thisTerm.key * otherTerm.key
            result += basisProduct * (thisTerm.value*otherTerm.value)
        }
    }
    return result
}

// Given a canonical operator, S, will create a mapping from
// agent states, d, to Operators such that d -> a^-_d[a*_d,S]
//
// Uses the identity
// [a*,a^m] = -ma^(m-1)
// so
// a^-_d[a*,a^m] = -m a^-_da^(m-1)
fun<AGENT> DoubleVector<Operator<AGENT>>.toCreationCommutationMap() : HashMap<AGENT, HashDoubleVector<Operator<AGENT>>> {
    val commutations = HashMap<AGENT, HashDoubleVector<Operator<AGENT>>>()
    forEach { entry ->
        val basis = entry.key
        val indices = basis.annihilations.keys
        indices.forEach {d ->
            val annihilationsminus1d = HashMap(basis.annihilations)
            val m = annihilationsminus1d.merge(d, -1) {a,b ->
                val sum = a+b
                if(sum == 0) null else sum
            }?:0
            val creationsminus1d = HashMap(basis.creations)
            creationsminus1d.merge(d, -1) {a,b ->
                val sum = a+b
                if(sum == 0) null else sum
            }
            val mdminus1 = Operator(creationsminus1d, annihilationsminus1d)
            commutations.getOrPut(d, { HashDoubleVector() })[mdminus1] = entry.value*-(m+1)
        }
    }
    return commutations
}


fun<BASIS : FockBasisVector<*, BASIS>> MutableDoubleVector<BASIS>.integrate(hamiltonian : (DoubleVector<BASIS>)-> DoubleVector<BASIS>, T : Double, dt : Double) {
    var time = 0.0
    while(time < T) {
        this += hamiltonian(this)*dt
        time += dt
    }
}
