package deselby.fockSpace

import deselby.fockSpace.extensions.*
import deselby.std.abstractAlgebra.HasTimes
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector

open class OperatorBasis<AGENT>(val creations: CreationBasis<AGENT> = CreationBasis(),
                                val annihilations: AnnihilationBasis<AGENT> = AnnihilationBasis()) :
        HasTimes<OperatorBasis<AGENT>, OperatorVector<AGENT>> {

    companion object {
        fun <AGENT> identity() = OperatorBasis<AGENT>()
//        fun <AGENT> create(d: AGENT) = OperatorBasis(mapOf(d to 1), emptyMap())
//        fun <AGENT> annihilate(d: AGENT) = OperatorBasis(emptyMap(), mapOf(d to 1))
    }


    fun reduce(ground: GroundState<AGENT>) =
            creations * (annihilations * ground)


    operator fun times(fockState : Pair<CreationVector<AGENT>, GroundState<AGENT>>) : CreationVector<AGENT> {
        val result = HashDoubleVector<CreationBasis<AGENT>>()
        val ground = fockState.second
        fockState.first.forEach { otherTerm ->
            val commutedState = annihilations * otherTerm.key
            commutedState.forEach { commutedTerm ->
                val commutedAnnihilations = commutedTerm.annihilationSequence()
                val commutedCreations = commutedTerm.creationSequence()
                val coeff = commutedTerm.getCoeffProduct()
                result += this.creations * commutedCreations * (commutedAnnihilations * ground) * (otherTerm.value * coeff)
            }
        }
        return result
    }

    operator fun times(ground : GroundState<AGENT>) : LazyCreationVector<AGENT> {
        return creations * (annihilations * ground)
    }


    // aa*^m = a*^ma + [a,a*^m] = a*^ma + ma*^(m-1)
    fun annihilate(d: AGENT): OperatorVector<AGENT> {
        val m = creations[d]
        if(m == 0) return OneHotDoubleVector(OperatorBasis(creations, annihilations.annihilate(d)), 1.0)
        return HashDoubleVector(
                OperatorBasis(creations, annihilations.annihilate(d)) to 1.0,
                this.create(d,-1) to m.toDouble()
        )
    }


    override fun times(other: OperatorBasis<AGENT>): OperatorVector<AGENT> {
        val result = HashDoubleVector<OperatorBasis<AGENT>>()
        val commutedMiddle = this.annihilations * other.creations
        commutedMiddle.forEach {
            val term = it.toOneHotDoubleVector()
            term.basis.creations *= this.creations
            term.basis.annihilations *= other.annihilations
            result[term.basis] = term.coeff
        }
        return result
    }

    fun create(d: AGENT) = create(d, 1)

    fun create(d: AGENT, n: Int) = OperatorBasis(creations.create(d,n),annihilations)

//
//    fun create(newCreations: Map<AGENT, Int>): OperatorBasis<AGENT> {
//        val union = HashMap(creations)
//        newCreations.forEach {
//            union.merge(it.key, it.value) {a , b ->
//                val newVal = a + b
//                if(newVal == 0) null else newVal
//            }
//        }
//        return OperatorBasis(union, annihilations)
//    }


    fun toVector() = OneHotDoubleVector(this, 1.0)

    override fun hashCode(): Int {
        return creations.hashCode() xor annihilations.hashCode()
    }


    override fun equals(other: Any?): Boolean {
        if (other !is OperatorBasis<*>) return false
        return (creations == other.creations) && (annihilations == other.annihilations)
    }


    override fun toString(): String {
        return creations.toString() + annihilations.toString()
    }

}