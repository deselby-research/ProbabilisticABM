package deselby.fockSpace

import deselby.fockSpace.extensions.allTermsContaining
import deselby.fockSpace.extensions.vectorMultiply
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector
import java.io.Serializable

abstract class Basis<AGENT>(val creations : Map<AGENT,Int>): Serializable {

    open val annihilations: Map<AGENT,Int>
        get() = emptyMap()

//    abstract fun multiplyTo(otherBasis: CreationBasis<AGENT>,
//                            ground: Ground<AGENT>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit)
//    abstract fun multiplyTo(groundBasis: GroundedBasis<AGENT,Ground<AGENT>>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit)
    abstract fun create(d: AGENT, n: Int=1): Basis<AGENT>
    abstract fun create(entries: Iterable<Map.Entry<AGENT,Int>>): Basis<AGENT>
    abstract fun timesAnnihilate(d: AGENT): Basis<AGENT>  // this * annihilation operator

    // if this = ca, c = creations and a=annihilations
    // returns other^-1[a,other]
    abstract fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer:(Basis<AGENT>, Double) -> Unit)

//    fun multiply(otherBasis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
//        termConsumer(otherBasis * this, 1.0)
//        semicommute(otherBasis, termConsumer)
//    }

    open fun map(transform: (AGENT) -> AGENT) : Basis<AGENT> {
        return newBasis(creations.mapKeys { transform(it.key) }, annihilations.mapKeys { transform(it.key) })
    }

    fun multiply(otherBasis: Basis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        termConsumer(this.union(otherBasis), 1.0)
        semicommute(otherBasis, termConsumer)
    }


    fun multiplyAndStrip(rhs: Basis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        termConsumer(newBasis(emptyMap(), this.annihilations.times(rhs.annihilations)), 1.0)
        semiCommuteAndStrip(rhs, termConsumer)
    }

    fun multiply(ground: Ground<AGENT>, termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
        ground.preMultiply(this, termConsumer)
    }

    inline fun multiplyAndGround(rhs: CreationBasis<AGENT>, ground: Ground<AGENT>, crossinline termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
        multiply(rhs) { prodBasis, prodWeight ->
            ground.preMultiply(prodBasis) { groundedBasis, groundedWeight ->
                termConsumer(groundedBasis, groundedWeight * prodWeight)
            }
        }
    }

    inline fun multiplyGroundAndMarginalise(rhs: CreationBasis<AGENT>, ground: Ground<AGENT>, activeAgents: Set<AGENT>, crossinline termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
        // TODO: Make this more efficient by marginalising sooner
        multiply(rhs) { prodBasis, prodWeight ->
            ground.preMultiply(prodBasis) { groundedBasis, groundedWeight ->
                termConsumer(groundedBasis.marginalise(activeAgents), groundedWeight * prodWeight)
            }
        }
    }


    operator fun times(rhs: CreationVector<AGENT>) : FockVector<AGENT> {
        val result = HashDoubleVector<Basis<AGENT>>()
        rhs.entries.forEach { rhsTerm ->
            this.multiply(rhsTerm.key) { basis, weight ->
                result.plusAssign(basis, weight*rhsTerm.value)
            }
        }
        return result
    }


    // Performs a semi-commutation which commutes the lhs annihilations over the rhs creations:
    //
    // ca semicommute CA = c[a,C]A
    //
    // where c and C are creations and a and A are annihilations.
    // This can be used to multiply two bases to cannonical form since ca * CA = cCaA + c[a,C]A
    //
    // Uses commuteToPerturbation which calculates (C^-1)[a,C]
    inline fun semicommute(other: Basis<AGENT>, crossinline termConsumer:(Basis<AGENT>, Double) -> Unit) {
        commuteToPerturbation(CreationBasis(other.creations)) { perturbation, weight ->
            val newCreations = HashMap(this.creations)
            newCreations.timesAssign(other.creations)
            newCreations.timesAssign(perturbation.creations)
            termConsumer(newBasis(newCreations, perturbation.annihilations.times(other.annihilations)), weight)
        }
    }


    // ca semicommute CA = c[a,C]A
    fun semicommute(vector: DoubleVector<Basis<AGENT>>) : FockVector<AGENT> = vectorMultiply(this, vector, Basis<AGENT>::semicommute)


    // ca semicommute CA = c[a,C]A
    inline fun semicommute(creationIndex: CreationIndex<AGENT>, crossinline termConsumer: (Basis<AGENT>, Double) -> Unit) {
        creationIndex.allTermsContaining(this.annihilations.keys).forEach { (indexedBasis, indexedWeight) ->
            this.semicommute(indexedBasis) { commutedBasis, commutedWeight ->
                termConsumer(commutedBasis, commutedWeight * indexedWeight)
            }
        }
    }


    // calculates [this, other] = [ca,CA]
    // uses the identity [ca,CA] = c[a,C]A - C[A,c]a
    fun commute(other: Basis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        this.semicommute(other, termConsumer)
        other.semicommute(this) { basis, weight -> termConsumer(basis, -weight) }
    }


    fun commute(vector: DoubleVector<Basis<AGENT>>) : FockVector<AGENT> = vectorMultiply(this, vector, Basis<AGENT>::commute)


    inline fun semiCommuteAndStrip(creationIndex: CreationIndex<AGENT>, crossinline termConsumer: (Basis<AGENT>, Double) -> Unit) {
        creationIndex.allTermsContaining(this.annihilations.keys).forEach { (indexedBasis, indexedWeight) ->
            this.semiCommuteAndStrip(indexedBasis) { commutedBasis, commutedWeight ->
                termConsumer(commutedBasis, commutedWeight * indexedWeight)
            }
        }
    }

    fun semiCommuteAndStrip(rhs: Basis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        val creationOrder = rhs.creations.values.sum()
        if(creationOrder > 2) TODO("Not implemented")
        val ann = this.annihilations
        when {
            creationOrder == 0 -> return

            creationOrder == 1 -> {
                val d = rhs.creations.keys.first()
                val m = ann[d]
                if (m != null) {
                    val annUnion = HashMap<AGENT,Int>(rhs.annihilations)
                    annUnion.timesAssign(ann)
                    annUnion.timesAssign(d, -1)
                    termConsumer(newBasis(emptyMap(), annUnion), m.toDouble())
                }
            }

            rhs.creations.size == 2 -> {
                val d1 = rhs.creations.keys.first()
                val m1 = ann[d1]
                val d2 = rhs.creations.keys.last()
                val m2 = ann[d2]
                if (m1 != null) {
                    val annUnion = HashMap<AGENT,Int>(rhs.annihilations)
                    annUnion.timesAssign(ann)
                    val annminusd1 = annUnion.times(d1, -1)
                    termConsumer(newBasis(emptyMap(), annminusd1), m1.toDouble())
                    if (m2 != null) {
                        termConsumer(newBasis(emptyMap(), annUnion.times(d2, -1)), m2.toDouble())
                        termConsumer(newBasis(emptyMap(), annminusd1.times(d2, -1)), (m1 * m2).toDouble())
                    }
                } else if (m2 != null) {
                    val annUnion = HashMap<AGENT,Int>(rhs.annihilations)
                    annUnion.timesAssign(ann)
                    annUnion.timesAssign(d2, -1)
                    termConsumer(newBasis(emptyMap(), annUnion), m2.toDouble())
                }
            }

            else -> {
                // must be reflexive
                val d = rhs.creations.keys.first()
                val m = ann[d]
                if (m != null) {
                    val annUnion = HashMap<AGENT, Int>(rhs.annihilations)
                    annUnion.timesAssign(ann)
                    termConsumer(newBasis(emptyMap(), annUnion.times(d, -1)), 2.0 * m)
                    if (m > 1) termConsumer(newBasis(emptyMap(), annUnion.times(d, -2)), (m * (m - 1)).toDouble())
                }
            }
        }
    }


    fun commuteAndGround(rhs: CreationBasis<AGENT>, D0: Ground<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {

    }


    open fun union(other: Basis<AGENT>) : Basis<AGENT> {
        return newBasis(this.creations * other.creations, this.annihilations * other.annihilations)
    }


    fun annihilate(d: AGENT): FockVector<AGENT> {
        val m = creations[d]?:0
        return(HashDoubleVector(
                this.create(d,-1) to m.toDouble(),
                this.timesAnnihilate(d) to 1.0
                ))
    }


    fun toVector(weight: Double = 1.0): FockVector<AGENT> = OneHotDoubleVector(this, weight)

    override fun toString(): String {
        return buildString {
            for (c in creations) {
                if (c.value == 1) append("a*(${c.key})") else append("a*(${c.key})^${c.value}")
            }
        }
    }


    companion object {
        fun<AGENT> identity() = CreationBasis<AGENT>(emptyMap())

        fun<AGENT> identityVector() = identity<AGENT>().toVector()

        fun<AGENT> identityCreationVector() = identity<AGENT>().toCreationVector()

        fun<AGENT> newBasis(creations: Collection<AGENT>, annihilations: Collection<AGENT>) =
                newBasis(creations.toCountMap(), annihilations.toCountMap())

        fun<AGENT> newBasis(creations: Map<AGENT,Int>, annihilations: Map<AGENT,Int>) : Basis<AGENT> {
            val nAnnihilations = annihilations.values.sum()
            return when(nAnnihilations) {
                0 -> CreationBasis(creations)
                1 -> ActionBasis(creations, annihilations.keys.first())
                2 -> if(annihilations.entries.size == 1)
                    ReflexiveBasis(creations, annihilations.keys.first())
                else
                    InteractionBasis(creations, annihilations.keys.first(), annihilations.keys.last())

                else -> OperatorBasis(creations, annihilations)
            }
        }

        fun<AGENT> create(d: AGENT) = CreationBasis(mapOf(d to 1))

        fun<AGENT> annihilate(d: AGENT) = ActionBasis(emptyMap(), d)

        inline fun<AGENT> Collection<AGENT>.toCountMap(): Map<AGENT,Int> {
            return when(size) {
                0 -> emptyMap()
                1 -> mapOf(first() to 1)
                else -> {
                    val map = HashMap<AGENT,Int>()
                    this.forEach { map.timesAssign(it, 1) }
                    map
                }
            }
        }

        inline operator fun<AGENT> Map<AGENT, Int>.times(other: Map<AGENT, Int>): Map<AGENT, Int> {
            val union = HashMap<AGENT,Int>(this)
            union *= other
            return union
        }


        inline fun<AGENT> Map<AGENT, Int>.times(vararg entries: Pair<AGENT,Int>): Map<AGENT, Int> {
            val union = HashMap(this)
            entries.forEach { union.timesAssign(it.first, it.second) }
            return union
        }

        inline operator fun<AGENT> Map<AGENT, Int>.times(entries: Iterable<Map.Entry<AGENT,Int>>): Map<AGENT, Int> {
            val union = HashMap(this)
            entries.forEach { union.timesAssign(it.key, it.value) }
            return union
        }


        inline fun<AGENT> Map<AGENT, Int>.times(d: AGENT, n: Int): Map<AGENT, Int> {
            val union = HashMap(this)
            union.timesAssign(d,n)
            return union
        }

        inline operator fun<AGENT> MutableMap<AGENT, Int>.timesAssign(other: Map<AGENT, Int>) =
            other.forEach { timesAssign(it.key, it.value) }


        inline fun<AGENT> MutableMap<AGENT, Int>.timesAssign(d: AGENT, n: Int) {
            merge(d, n) { a, b ->
                val sum = a + b
                if (sum == 0) null else sum
            }
        }

    }
}