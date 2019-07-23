package experiments.fockBasis

import deselby.std.collections.hashMultisetOf

fun main() {
//    val deselby = SparseFockState(DeselbyBasis(mapOf(0 to 0.1)))
//    val fock = SparseFockState(DeltaBasis<Int>())
//    val fock = SparseFockState(OperatorBasis<Int>())
    val fock = CommutationRelation(OperatorBasis.create(0))

    println(fock)
    println("a* = ${fock.create(0)}")
    println("a = ${fock.annihilate(0)}")
    println("a*a* = ${fock.create(0).create(0)}")
    println("aa* = ${fock.create(0).annihilate(0)}")
    println("a*a = ${fock.annihilate(0).create(0)}")
    println("a*a*a = ${fock.annihilate(0).create(0).create(0)}")
    println("a*a*a - aa* = ${fock.annihilate(0).create(0).create(0) - fock.annihilate(0).create(0)}")
//    println(fock.annihilate(0)*deselby)
}
