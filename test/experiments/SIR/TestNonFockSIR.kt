package experiments.SIR

import deselby.std.gnuplot
import org.apache.commons.math3.random.MersenneTwister
import org.junit.Test

class TestNonFockSIR {
    @Test
    fun compareDifferentialDiscrete() {
        val bg = SIRParams(0.1,1.0, 0.0, 0.0)
        val S0 = 5000
        val I0 = 100
        val DT = 0.001
        val rand = MersenneTwister()

        val sim = NonFockSIR.SIRSimulator(bg, rand)
        val discreteData = sim.simulateAndObserve(NonFockSIR.SIRState(S0, I0, 0), DT, 100*DT)
        val differentialData = sim.simulateAndObserve(NonFockSIR.DoubleSIRState(S0.toDouble(), I0.toDouble(), 0.0), DT, 100*DT)

        gnuplot {
            val discreteI = heredoc(discreteData.asSequence().map {it.I})
            val differentialI = heredoc(differentialData.asSequence().map {it.I})
            val discreteS = heredoc(discreteData.asSequence().map {it.S})
            val differentialS = heredoc(differentialData.asSequence().map {it.S})
            invoke("""
                plot $discreteI with lines
                replot $differentialI with lines
                replot $discreteS with lines
                replot $differentialS with lines
            """)
        }
    }


    @Test
    fun test() {
    }
}