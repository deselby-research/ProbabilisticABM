package deselby.fockSpace

class MutableDeselbyBasis<AGENT>(lambda: Map<AGENT, Double>, override val creations: MutableMap<AGENT, Int> = HashMap()) : DeselbyBasis<AGENT>(lambda, creations) {

    constructor(other : DeselbyBasis<AGENT>) : this(other.lambda, HashMap(other.creations))

    fun createAssign(d : AGENT, n : Int) {
        creations.merge(d, n) { a,b ->
            val sum = a+b
            if(sum == 0) null else sum
        }
    }
}