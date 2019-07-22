package experiments.fockBasis

import deselby.std.collections.hashMultisetOf

fun main() {
    val deselby = SparseFockState(DeselbyBasis(mapOf(0 to 0.1)))
//    val fock = SparseFockState(DeltaBasis<Int>())
    val fock = SparseFockState(OperatorBasis<Int>())
//    val fock = CommutationRelation<Int>(FockOperator())
    println(fock)
    println(fock.create(0).create(0))
    println(fock.create(0))
    println(fock.create(0).annihilate(0))
    println(fock.annihilate(0))
    println(fock.annihilate(0).create(0))
    println(fock.annihilate(0).create(0).create(0))
    println(fock.annihilate(0).create(0).create(0) - fock.annihilate(0).create(0))
    println(fock.annihilate(0)*deselby)
}