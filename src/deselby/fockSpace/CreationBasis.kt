package deselby.fockSpace

import deselby.fockSpace.extensions.LazyCreationBasis
import deselby.std.abstractAlgebra.HasTimes
import deselby.std.vectorSpace.MutableDoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector

class CreationBasis<AGENT> :
        OperatorSet<AGENT>,
        LazyCreationBasis<AGENT>,
        HasTimes<LazyCreationBasis<AGENT>, CreationBasis<AGENT>>
{

    companion object {
        fun <AGENT> identity() = CreationBasis<AGENT>(HashMap())
        fun <AGENT> create(d: AGENT) = CreationBasis(hashMapOf(d to 1))
        fun <AGENT> remove(d: AGENT) = CreationBasis(hashMapOf(d to -1))
    }

    constructor(map: MutableMap<AGENT, Int> = HashMap()) : super(map)

    constructor(entries : LazyCreationBasis<AGENT>) : super(entries)

    override fun iterator() = map.entries.iterator()

    fun copyOf() = CreationBasis(HashMap(map))


    override operator fun times(other : LazyCreationBasis<AGENT>) : CreationBasis<AGENT> {
        val copy = copyOf()
        copy.unionAssign(other)
        return copy
    }

    operator fun times(other : AnnihilationBasis<AGENT>) : OperatorBasis<AGENT> {
        return OperatorBasis(this, other)
    }

    operator fun timesAssign(other: CreationBasis<AGENT>) {
        unionAssign(other)
    }


    fun create(d : AGENT, n : Int = 1) : CreationBasis<AGENT> {
        val copy = copyOf()
        copy.add(d,n)
        return copy
    }


    fun invoke(ground : GroundState<AGENT>) = Pair(this,ground)

    fun toVector() = OneHotDoubleVector(this, 1.0)


    override fun toString(): String {
        var s = ""
        for (c in map) {
            if (c.value == 1) s += "a*(${c.key})" else s += "a*(${c.key})^${c.value}"
        }
        return s
    }
}
//
//
//<AGENT>(val creations: Map<AGENT, Int>) :
//        HasTimes<CreationBasis<AGENT>, DoubleVector<CreationBasis<AGENT>>> {
//
//    companion object {
//        fun <AGENT> identity() = CreationBasis<AGENT>(emptyMap())
//        fun <AGENT> create(d: AGENT) = CreationBasis(mapOf(d to 1))
//        fun <AGENT> remove(d: AGENT) = CreationBasis(mapOf(d to -1))
//    }
//
//
//
//    // aa*^m = a*^ma + [a,a*^m] = a*^ma + ma*^(m-1)
//    fun annihilate(d: AGENT, ground : GroundState<AGENT>): MutableDoubleVector<CreationBasis<AGENT>> {
//        val m = this[d]
//        if(m == 0) return this * ground.annihilate(d)
//        val result = HashDoubleVector(this.create(d,-1) to m.toDouble())
//        result += this * ground.annihilate(d)
//        return result
//    }
//
//    // a^na*^m = a^n-1a*^ma + a^n-1[a,a*^m] = a^n-1a*^ma + ma^n-1a*^(m-1)
//    fun annihilate(d: AGENT, n : Int, ground : GroundState<AGENT>): MutableDoubleVector<CreationBasis<AGENT>> {
//        TODO()
//    }
//
//
//    operator fun times(a : Annihilate<AGENT>) : OperatorBasis<AGENT> {
//        return OperatorBasis(creations, mapOf(a.d to 1))
//    }
//
//
//    override fun times(other: CreationBasis<AGENT>): DoubleVector<CreationBasis<AGENT>> {
//        return OneHotDoubleVector(other.create(creations),1.0)
//    }
//
//    fun create(d: AGENT) = create(d, 1)
//
//    fun create(d: AGENT, n: Int): CreationBasis<AGENT> {
//        val newCreations = HashMap(creations)
//        newCreations.merge(d, n) {a , b ->
//            val newVal = a + b
//            if(newVal == 0) null else newVal
//        }
//        return CreationBasis(newCreations)
//    }
//
//    fun create(newCreations: Map<AGENT, Int>): CreationBasis<AGENT> {
//        val union = HashMap(creations)
//        newCreations.forEach {
//            union.merge(it.key, it.value) {a , b ->
//                val newVal = a + b
//                if(newVal == 0) null else newVal
//            }
//        }
//        return CreationBasis(union)
//    }
//
//
//    operator fun get(d: AGENT): Int {
//        return creations.getOrDefault(d, 0)
//    }
//
//
//    override fun hashCode(): Int {
//        return creations.hashCode()
//    }
//
//
//    override fun equals(other: Any?): Boolean {
//        if (other !is OperatorBasis<*>) return false
//        return (creations == other.creations)
//    }
//
//
//    override fun toString(): String {
//        var s = ""
//        for (c in creations) {
//            if (c.value == 1) s += "a*(${c.key})" else s += "a*(${c.key})^${c.value}"
//        }
//        return s
//    }
//}
