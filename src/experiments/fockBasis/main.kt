package experiments.fockBasis

fun main() {
//    val fock = SparseFockDecomposition(DeselbyGroundState(mapOf(0 to 0.1)))
    val fock = SparseFockDecomposition(DeltaGroundState<Int>())
//    val fock = SparseFockDecomposition(OperatorBasis<Int>())
//    val fock = CommutationRelation<Int>(FockOperator())
    println(fock.create(0).create(0))
    println(fock.create(0).annihilate(0))
    println(fock.annihilate(0).create(0))
}