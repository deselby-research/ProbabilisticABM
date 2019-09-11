package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector

abstract class Basis<AGENT>(val creations : Map<AGENT,Int>) {

//    abstract fun multiplyTo(otherBasis: CreationBasis<AGENT>,
//                            ground: Ground<AGENT>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit)
//    abstract fun multiplyTo(groundBasis: GroundedBasis<AGENT,Ground<AGENT>>,
//                            termConsumer: (CreationBasis<AGENT>, Double) -> Unit)
    abstract fun create(d: AGENT, n: Int=1): Basis<AGENT>
    abstract fun create(entries: Iterable<Map.Entry<AGENT,Int>>): Basis<AGENT>
    abstract fun timesAnnihilate(d: AGENT): Basis<AGENT>  // this * annihilation operator
    abstract fun commuteToPerturbation(basis: CreationBasis<AGENT>, termConsumer:(Basis<AGENT>, Double) -> Unit) // other^-1[this,other]
    abstract fun forEachAnnihilationKey(keyConsumer: (AGENT) -> Unit)
    abstract fun forEachAnnihilationEntry(entryConsumer: (AGENT,Int) -> Unit)

    fun multiply(otherBasis: CreationBasis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        termConsumer(otherBasis * this, 1.0)
        semicommute(otherBasis, termConsumer)
    }

    fun multiply(ground: Ground<AGENT>, termConsumer: (CreationBasis<AGENT>, Double) -> Unit) {
        ground.preMultiply(this, termConsumer)
    }


//    inline fun semicommute(basis: CreationBasis<AGENT>, crossinline termConsumer:(Basis<AGENT>, Double) -> Unit) {
//        commuteToPerturbation(basis) { perturbation, weight ->
//            termConsumer(basis * perturbation, weight)
//        }
//    }


    // Performs a semi-commutation which commutes the lhs annihilations over the rhs creations:
    //
    // ca semicommute CA = c[a,C]A
    //
    // where c and C are creations and a and A are annihilations.
    // This can be used to multiply two bases to cannonical form since ca * CA = cCaA + c[a,C]A
    //
    // Uses commuteToPerturbation which calculates (C^-1)c[a,C]
    inline fun semicommute(other: Basis<AGENT>, crossinline termConsumer:(Basis<AGENT>, Double) -> Unit) {
        commuteToPerturbation(CreationBasis(other.creations)) { perturbation, weight ->
            termConsumer(other.operatorUnion(perturbation), weight)
        }
    }


    // ca semicommute CA = c[a,C]A
    fun semicommute(vector: DoubleVector<Basis<AGENT>>): DoubleVector<Basis<AGENT>> {
        val commutation = HashDoubleVector<Basis<AGENT>>()
        vector.forEach { (vBasis, vWeight) ->
            this.semicommute(vBasis) { commutedBasis, cWeight ->
                commutation.plusAssign(commutedBasis, cWeight *vWeight)
            }
        }
        return commutation
    }


    // calculates [this, other] = [ca,CA]
    // uses the identity [ca,CA] = c[a,C]A - C[A,c]a
    fun commute(other: Basis<AGENT>, termConsumer: (Basis<AGENT>, Double) -> Unit) {
        this.semicommute(other, termConsumer)
        other.semicommute(this) { basis, weight -> termConsumer(basis, -weight) }
    }


    fun operatorUnion(other: CreationBasis<AGENT>) : Basis<AGENT> {
        return newBasis(this.creations * other.creations, this.toAnnihilationMap())
    }


    open fun operatorUnion(other: Basis<AGENT>) : Basis<AGENT> {
        val annihilations = HashMap<AGENT,Int>()
        forEachAnnihilationEntry { agent, count ->
            annihilations[agent] = count
        }
        other.forEachAnnihilationEntry {agent, count ->
            annihilations.timesAssign(agent, count)
        }
        return newBasis(this.creations * other.creations, annihilations)
    }


    open fun toAnnihilationMap(): Map<AGENT,Int> {
        val annihilations = HashMap<AGENT,Int>()
        forEachAnnihilationEntry { agent, count ->
            annihilations[agent] = count
        }
        return annihilations
    }


    fun annihilate(d: AGENT): FockVector<AGENT> {
        val m = creations[d]?:0
        return(HashDoubleVector(
                this.create(d,-1) to m.toDouble(),
                this.timesAnnihilate(d) to 1.0
                ))
    }


    fun toVector(weight: Double = 1.0) = OneHotDoubleVector(this, weight)

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