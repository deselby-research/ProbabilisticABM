package deselby.fockSpace.extensions

import deselby.fockSpace.AnnihilationBasis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.GroundState
import deselby.fockSpace.OperatorBasis
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector
import java.util.AbstractMap


fun<AGENT> List<AnnihilationBasis.OperatorCount<AGENT>>.toOneHotDoubleVector() : OneHotDoubleVector<OperatorBasis<AGENT>> {
    val newBasis = OperatorBasis<AGENT>()
    var coeff = 1.0
    forEach {
            newBasis.creations[it.agent] = it.nCreations
            newBasis.annihilations[it.agent] = it.nAnnihilations
            coeff *= it.coeff
    }
    return OneHotDoubleVector(newBasis, coeff)
}



fun<AGENT> List<AnnihilationBasis.OperatorCount<AGENT>>.creationSequence(): LazyCreationBasis<AGENT> {
    return this.asSequence().map {
        AbstractMap.SimpleEntry(it.agent, it.nCreations)
    }.asCreationSequence()
}


fun<AGENT> List<AnnihilationBasis.OperatorCount<AGENT>>.annihilationSequence(): LazyAnnihilationBasis<AGENT> {
    return this.asSequence().map {
        AbstractMap.SimpleEntry(it.agent, it.nAnnihilations)
    }.asAnnihilationSequence()
}

fun<AGENT> List<AnnihilationBasis.OperatorCount<AGENT>>.getCoeffProduct(): Double {
    return this.asSequence().fold(1.0) {prod, term ->
        prod * term.coeff
    }
}


fun<AGENT> Sequence<Map.Entry<AGENT,Int>>.asAnnihilationSequence() : LazyAnnihilationBasis<AGENT> {
    return object : LazyAnnihilationBasis<AGENT> {
        override fun iterator() = this@asAnnihilationSequence.iterator()
    }
}

fun<AGENT> Sequence<Map.Entry<AGENT,Int>>.asCreationSequence() : LazyCreationBasis<AGENT> {
    return object : LazyCreationBasis<AGENT> {
        override fun iterator() = this@asCreationSequence.iterator()
    }
}


operator fun<AGENT> LazyCreationBasis<AGENT>.times(basis: LazyCreationBasis<AGENT>) : CreationBasis<AGENT> {
    val union = CreationBasis(basis)
    forEach { union.add(it.key, it.value) }
    return union
}


operator fun<AGENT> LazyAnnihilationBasis<AGENT>.times(basis: LazyAnnihilationBasis<AGENT>) : AnnihilationBasis<AGENT> {
    val union = AnnihilationBasis(basis)
    forEach { union.add(it.key, it.value) }
    return union
}

operator fun<AGENT> LazyCreationVector<AGENT>.times(other: LazyCreationBasis<AGENT>) = other * this
operator fun<AGENT> LazyCreationBasis<AGENT>.times(basis: LazyCreationVector<AGENT>) : LazyCreationVector<AGENT> {
    return basis.map { AbstractMap.SimpleEntry(this * it.key, it.value) }
}


operator fun<AGENT> LazyAnnihilationBasis<AGENT>.times(ground : GroundState<AGENT>) : DoubleVector<CreationBasis<AGENT>> {
    return ground.annihilate(this)
}

operator fun<AGENT> LazyAnnihilationVector<AGENT>.times(other: LazyAnnihilationBasis<AGENT>) = other * this
operator fun<AGENT> LazyAnnihilationBasis<AGENT>.times(other: LazyAnnihilationVector<AGENT>) =
        other.map { AbstractMap.SimpleEntry(this * it.key, it.value) }