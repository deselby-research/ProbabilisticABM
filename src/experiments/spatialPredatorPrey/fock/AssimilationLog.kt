package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.BinomialBasis
import deselby.fockSpace.CreationBasis
import deselby.fockSpace.DeselbyGround
import deselby.fockSpace.GroundedBasis
import deselby.std.Gnuplot
import deselby.std.gnuplot
import experiments.spatialPredatorPrey.Params
import java.io.FileInputStream
import java.io.ObjectInputStream
import kotlin.math.max
import kotlin.math.pow

class AssimilationLog {

    data class Record(
            val realState: Map<Agent,Int>,
            val observation: BinomialBasis<Agent>,
            val posteriorState: CreationBasis<Agent>,
            val posteriorGround: DeselbyGround<Agent>
            )

    val params: Params
    val records = ArrayList<Record>()

    constructor(filename: String) {
        val fileIn = FileInputStream(filename)
        val objIn = ObjectInputStream(fileIn)
        params =  objIn.readObject() as Params
        println("available = ${fileIn.available()}")
        while(fileIn.available() > 0) {
            val realStates = objIn.readObject() as Map<Agent,Int>
            val observation = objIn.readObject() as BinomialBasis<Agent>
            val posteriorState = objIn.readObject() as CreationBasis<Agent>
            val posteriorGround = objIn.readObject() as DeselbyGround<Agent>
            records.add(Record(realStates, observation, posteriorState, posteriorGround))
        }
    }

    fun plot(nRecord: Int) {
        val gridSize = params.GRIDSIZE
        val realState = records[nRecord].realState
        val state = records[nRecord].posteriorState.asGroundedBasis(records[nRecord].posteriorGround)


//        val rabbitDensity = state.ground.lambdas.asSequence()
//                .filter { it.key is Prey }.associateBy({ it.key.pos },{ max(state.basis[it.key].toDouble(), it.value).pow(0.5) })
//
//        val foxDensity = state.ground.lambdas.asSequence()
//                .filter { it.key is Predator }.associateBy({ it.key.pos },{ max(state.basis[it.key].toDouble(), it.value).pow(0.5) })

        val rabbitDensity = state.ground.lambdas.asSequence()
                .filter { it.key is Prey }.associateBy({ it.key.pos },{if(state.basis[it.key] > 0) -1.0 else it.value.pow(0.5) })

        val foxDensity = state.ground.lambdas.asSequence()
                .filter { it.key is Predator }.associateBy({ it.key.pos },{if(state.basis[it.key] > 0) -1.0 else it.value.pow(0.5) })


        val rabbitRange = rabbitDensity.values.max()?:1.0
        val foxRange = foxDensity.values.max()?:1.0

        println("$rabbitRange $foxRange")

        val colourData = rabbitDensity.entries.asSequence().flatMap { (pos, rabbitDensity) ->
            val foxD = foxDensity[pos]?:0.0
            sequenceOf<Number>(
                    pos.rem(gridSize),
                    pos.div(gridSize),
                    if(rabbitDensity < 0.0) 255 else 255*rabbitDensity/rabbitRange,
                    0,
                    if(foxD < 0.0) 255 else 255*foxD/foxRange)
        }

//        val rabbitData  = state.ground.lambdas.asSequence()
//                .filter { it.key is Prey }
//                .flatMap { (agent, lambda) ->
//                    sequenceOf<Number>(agent.pos.rem(gridSize), agent.pos.div(gridSize), (state.basis[agent] + lambda)*255.0/maxMean, 255, 255, 128)
//                }
//
//        val foxData = state.ground.lambdas.asSequence()
//                .filter { it.key is Predator }
//                .flatMap { (agent, lambda) ->
//                    sequenceOf<Number>(agent.pos.rem(gridSize), agent.pos.div(gridSize), 255, 255, (state.basis[agent] + lambda)*255.0/maxMean, 128)
//                }

        val realData = realState.entries.asSequence().flatMap {(agent, _) ->
            sequenceOf(agent.pos.rem(gridSize), agent.pos.div(gridSize), if(agent is Prey) 1 else 2)
        }.toList()

        gnuplot {
//            val rData = heredoc(rabbitData,6)
//            val fData = heredoc(foxData,6)
            val rgb = heredoc(colourData, 5)
            invoke("""
                set linetype 1 lc 'red'
                set linetype 2 lc 'blue'
                unset xtics
                unset ytics
                plot $rgb with rgbimage title ""
            """)
            if(realData.isNotEmpty()) {
                val pointData = heredoc(realData, 3)
                invoke("""
                    replot $pointData with points pointtype 5 pointsize 0.5 lc variable title ""
                    """)
            }
        }
    }


}