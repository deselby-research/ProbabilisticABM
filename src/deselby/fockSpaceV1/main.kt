package deselby.fockSpaceV1

import deselby.fockSpaceV2.extensions.annihilate
import deselby.fockSpaceV2.FockBasisVector
import deselby.fockSpaceV2.Operator
import deselby.fockSpaceV2.extensions.create
import deselby.fockSpaceV2.extensions.toCreationCommutationMap
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.OneHotDoubleVector


fun main() {
//    val basis = DeselbyBasis(mapOf(0 to 0.1)).create(0,2)
//    val fock = DeselbyPerturbationBasis(basis as DeselbyBasis<Int>).toFockState()
//    val denseDeselby = SparseFockState(DeselbyBasis(mapOf(0 to 0.1)))
//    val fock = SparseFockState(DeltaBasis<Int>())
//    val fock = SparseFockState(OperatorBasis.identity<Int>())
//    val fock = CommutationRelation(OperatorBasis.create(0))
//    val pb = DeselbyPerturbationBasis(basis as DeselbyBasis<Int>)
//    val fock = OneHotFock(DeselbyPerturbationBasis(DeselbyBasis(mapOf(0 to 0.1))))

//    val fock = OneHotDoubleVector(Deselby(mapOf(0 to 0.1)),1.0)
    val fock = OneHotDoubleVector(Operator.identity<Int>(),1.0)

    testOperators(fock)

    val hamiltonian = H(fock)
    println(hamiltonian)
    println(hamiltonian.toCreationCommutationMap())
}


fun <T : FockState<Int, T>> H(s :T) : T {
    val a = s.annihilate(0).create(0)
    return a.create(0) - a
}

fun <T : FockBasisVector<Int, T>> H(s :DoubleVector<T>) : DoubleVector<T> {
    val a = s.annihilate(0).create(0)
    return a.create(0) - a
}


fun <T : FockState<Int, T>> multidimH(s :T) : T {
    val a = s.annihilate(0).create(1) - s.annihilate(0).create(0)
    val b = s.annihilate(0).annihilate(0).create(1) - s.annihilate(1).create(1)
//    val b = s.annihilate(1).annihilate(0).create(1) - s.annihilate(1).create(1) + s.annihilate(0).create(1)
    return a+b
}


fun <T : FockState<Int, T>> testOperators(fock : T) {
    println("fock = $fock")
    println("a* = ${fock.create(0)}")
    println("a = ${fock.annihilate(0)}")
    println("a*a* = ${fock.create(0).create(0)}")
    println("aa* = ${fock.create(0).annihilate(0)}")
    println("a*a = ${fock.annihilate(0).create(0)}")
    println("a*a*a = ${fock.annihilate(0).create(0).create(0)}")
    println("a*a*a - aa* = ${fock.annihilate(0).create(0).create(0) - fock.annihilate(0).create(0)}")

}

fun <T : FockBasisVector<Int, T>> testOperators(fock : DoubleVector<T>) {
    println("fock = $fock")
    println("a* = ${fock.create(0)}")
    println("a = ${fock.annihilate(0)}")
    println("a*a* = ${fock.create(0).create(0)}")
    println("aa* = ${fock.create(0).annihilate(0)}")
    println("a*a = ${fock.annihilate(0).create(0)}")
    println("a*a*a = ${fock.annihilate(0).create(0).create(0)}")
    println("a*a*a - aa* = ${fock.annihilate(0).create(0).create(0) - fock.annihilate(0).create(0)}")
}


fun testMonteCarlo() {
    val fock = SparseFockState(OperatorBasis.identity<Int>())

    var sampleBasis = MutableDeselbyBasis(mapOf(0 to 0.1, 1 to 0.2), hashMapOf(0 to 1))
    val sample = OneHotFock(sampleBasis)
    val H = multidimH(fock)
    println(H)
    val commutations = CreationCommutations(H)
    println(commutations.index)
    val possibleTransitionStates = SamplableFockState(H * DeselbyPerturbationBasis(sampleBasis).toFockState())

    for(step in 1..10) {
        // choose a perturbation
        val perturbation = possibleTransitionStates.sample()
        val basis = perturbation.basis as DeselbyPerturbationBasis<Int>
        // apply it to state
        basis.creations.forEach {
            val commutation: MapFockState<Int> = commutations[it.key]?: ZeroFockState()
            if (it.value > 0) {
                for(i in 1..it.value) {
                    val Q = commutation * sample
                    possibleTransitionStates -= Q
                    sampleBasis.createAssign(it.key, 1)
                }
            } else if (it.value < 0) {
                for(i in 1..-it.value) {
                    sampleBasis.createAssign(it.key, -1)
                    val Q = commutation * sample
                    possibleTransitionStates += Q
                }
            }
        }
        println(sampleBasis)
    }


}