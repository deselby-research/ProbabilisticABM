import deselby.ProbabilisticSet
import deselby.operators.Annihilate
import deselby.operators.Create

fun main(args : Array<String>) {
    var probABM = ProbabilisticSet<Int>()

    Create(1) * Create(2) * Annihilate(1)

}