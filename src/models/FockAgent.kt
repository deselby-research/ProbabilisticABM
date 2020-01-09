package models

import deselby.fockSpace.ActionBasis
import deselby.fockSpace.Basis
import deselby.fockSpace.FockVector
import deselby.fockSpace.extensions.times

object FockAgent {

    fun<AGENT> action(subjectAgent: AGENT, rate: Double, vararg resultAgents: AGENT) : FockVector<AGENT> {
        val from = ActionBasis(mapOf(subjectAgent to 1), subjectAgent)
        val to = ActionBasis(resultAgents.asList(), subjectAgent)
        return (to.toVector() - from.toVector())*rate
    }

    // Only works for max occupation number of 1
    fun<AGENT> actionInAbsence(subjectAgent: AGENT, absentAgent: AGENT, rate: Double, vararg resultAgents: AGENT) : FockVector<AGENT> {
        val action = action(subjectAgent, rate, *resultAgents)
        val absence = ActionBasis(mapOf(absentAgent to 1), absentAgent).toVector()
        return action - action*absence
    }


    fun<AGENT> interaction(subjectAgent: AGENT, objectAgent: AGENT, rate: Double, vararg resultAgents: AGENT) : FockVector<AGENT> {
        val thisAndOther = listOf(subjectAgent, objectAgent)
        val from = Basis.newBasis(thisAndOther, thisAndOther)
        val to = Basis.newBasis(resultAgents.asList(), thisAndOther)
        return (to.toVector() - from.toVector())*rate
    }
}