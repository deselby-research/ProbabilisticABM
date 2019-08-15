package experiments.SIR

import deselby.std.Gnuplot
import org.apache.commons.math3.random.MersenneTwister
import org.junit.Test

class TestNonFockSIR {
    @Test
    fun compareDifferentialDiscrete() {
        val beta = 0.1
        val gamma = 1.0
        val S0 = 5000
        val I0 = 100
        val DT = 0.001
        val rand = MersenneTwister()

        val params = NonFockSIR.SIRParams(beta, gamma, rand)
        val discreteData = params.simulateAndObserve(NonFockSIR.SIRState(S0, I0, 0), DT, 100*DT)
        val differentialData = params.simulateAndObserve(NonFockSIR.DoubleSIRState(S0.toDouble(), I0.toDouble(), 0.0), DT, 100*DT)

        Gnuplot()
                .plot(discreteData.asSequence().map {it.I})
                .replot(differentialData.asSequence().map {it.I})
                .replot(differentialData.asSequence().map {it.S})
                .replot(discreteData.asSequence().map {it.S})
                .close()
    }


    @Test
    fun test() {
    }
}