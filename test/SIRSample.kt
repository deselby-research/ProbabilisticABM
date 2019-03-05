import deselby.Action
import deselby.Behaviour
import deselby.Interaction
import deselby.PABMSample

enum class SIRAgent {
    S,
    I,
    R
}

class DifferentialSIR(val alpha : Double, val beta : Double, var S : Double, var I : Double, var R : Double) {
    fun step(dt : Double) {
        val dS_dt = -beta*S*I
        val dR_dt = alpha*I
        val dI_dt = -(dS_dt + dR_dt)
        S += dS_dt*dt
        I += dI_dt*dt
        R += dR_dt*dt
    }
}

fun main(args : Array<String>) {
    val alpha = 1.0
    val beta = 0.1
    val myABM = PABMSample<SIRAgent>()
    val behaviour = Behaviour<SIRAgent>(arrayOf(
            Action(alpha, {it == SIRAgent.I}, {agent, pabm ->
                pabm.add(SIRAgent.R)
            }),
            Interaction(beta, {it == SIRAgent.I}, {it == SIRAgent.S}, {subj, obj, pabm ->
                pabm.add(SIRAgent.I)
                pabm.add(SIRAgent.I)
            })
    ))

    val S0 = 500
    val I0 = 10
    myABM.add(SIRAgent.S,S0)
    myABM.add(SIRAgent.I, I0)
    myABM.setBehaviour(behaviour)

    val dsir = DifferentialSIR(alpha, beta, S0.toDouble(), I0.toDouble(), 0.0)
    val dt = 1.0/16.0
    for(i in 1..100) {
        myABM.integrate(dt)
        for(d in 1..16) dsir.step(dt/16.0)
        println("${myABM.agents[SIRAgent.S]}\t${myABM.agents[SIRAgent.I]}\t${myABM.agents[SIRAgent.R]}\t${dsir.S}\t${dsir.I}\t${dsir.R}")
    }
}