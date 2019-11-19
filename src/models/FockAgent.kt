package models

import deselby.fockSpace.ActionBasis
import deselby.fockSpace.Basis
import deselby.fockSpace.FockVector

object FockAgent {

    fun<AGENT> action(subjectAgent: AGENT, rate: Double, vararg resultAgents: AGENT) : FockVector<AGENT> {
        val from = ActionBasis(mapOf(subjectAgent to 1), subjectAgent)
        val to = ActionBasis(resultAgents.asList(), subjectAgent)
        return (to.toVector() - from.toVector())*rate
    }


    fun<AGENT> interaction(subjectAgent: AGENT, objectAgent: AGENT, rate: Double, vararg resultAgents: AGENT) : FockVector<AGENT> {
        val thisAndOther = listOf(subjectAgent, objectAgent)
        val from = Basis.newBasis(thisAndOther, thisAndOther)
        val to = Basis.newBasis(resultAgents.asList(), thisAndOther)
        return (to.toVector() - from.toVector())*rate
    }
}