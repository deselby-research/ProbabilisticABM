package experiments.spatialPredatorPrey.discreteEventABM

abstract class Agent {
    var id: Int
    val SIZE: Int

    var nextEvent :Simulation.Event? = null
//    val SIZE = Simulation.GRIDSIZE
    var xPos: Int
        get() = id.rem(SIZE)
        set(x: Int) {
            id = yPos*SIZE + (x+SIZE).rem(SIZE)
        }
    var yPos: Int
        get() = id.div(SIZE)
        set(y: Int) {
            id = xPos + (y+SIZE).rem(SIZE)*SIZE
        }


    abstract fun scheduleNextEvent(sim: Simulation)
    abstract fun executeEvent(sim: Simulation)
//    abstract fun fockId(): Int


    constructor(id: Int, SIZE: Int) {
        this.id = id
        this.SIZE = SIZE
    }

    constructor(xPos: Int, yPos: Int, gridSize: Int): this(
            (xPos+gridSize).rem(gridSize) + gridSize*(yPos+gridSize).rem(gridSize), gridSize)


    fun diffuse(sim: Simulation) {
        sim.remove(this)
        when(sim.rand.nextInt(4)) {
            0 -> xPos = xPos + 1
            1 -> xPos = xPos - 1
            2 -> yPos = yPos + 1
            3 -> yPos = yPos - 1
        }
        sim.add(this)
    }


    fun die(sim: Simulation) {
        sim.remove(this)
    }


    fun sphereOfInfluence(): List<Int> {
        val S = SIZE
        val x = id.rem(S)
        val y = id.div(S)
        return listOf(
                (x+1).rem(S) + y*S,
                (x+S-1).rem(S) + y*S,
                x + (y + 1).rem(S)*S,
                x + (y + S - 1).rem(S)*S
        )
    }
}