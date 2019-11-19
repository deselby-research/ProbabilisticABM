package models.pedestrians

class Params(val GRIDSIZE: Int,
             val rUp: Double,
             val rHorizontal: Double) {
    val GRIDSIZESQ: Int
        get() = GRIDSIZE*GRIDSIZE
}