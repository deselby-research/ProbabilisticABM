package experiments.spatialPredatorPrey

open class Params(val GRIDSIZE: Int,
             val lambdaPred: Double,
             val lambdaPrey: Double,
             val preyDie: Double,
             val preyReproduce: Double,
             val preyDiffuse: Double,
             val predDie: Double,
             val predCaptureOnly: Double,
             val predCaptureAndReproduce: Double,
             val predDiffuse: Double) {
    val GRIDSIZESQ = GRIDSIZE*GRIDSIZE
    val predCapture = predCaptureAndReproduce + predCaptureOnly
    val preyTotal = preyDie + preyDiffuse + preyReproduce
}

object StandardParams : Params(
        32,
        0.02,
        0.04,
        0.03,
        0.06,
        1.0,
        0.07,
        0.05,
        0.5,
        1.0
)

object SmallParams : Params(
        2,
        0.25,
        0.2,
        0.03,
        0.06,
        1.0,
        0.07,
        0.05,
        0.5,
        1.0
)
