package experiments.spatialPredatorPrey.discreteEventABM

import deselby.std.Gnuplot
import deselby.std.extensions.nextPoisson
import org.apache.commons.math3.random.MersenneTwister
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max

class Simulation {
    companion object {
        const val GRIDSIZE = 3
        const val GRIDSIZESQ = GRIDSIZE* GRIDSIZE
    }

    data class Event(val time: Double, val agent: Agent) : Comparable<Event> {
        override fun compareTo(other: Event): Int {
            return time.compareTo(other.time)
//            if(dt == 0) {
 //               TODO("distinguish equal time events")
//                val dpos = agent.id.compareTo(other.agent.id)
//                if(dpos == 0) return (agent is Predator).compareTo(other.agent is Predator)
//                return dpos
//            }
//            return dt
        }
    }

    val positionIndex: Array<ArrayList<Agent>>
    val eventQueue = TreeSet<Event>()
    var time = 0.0
    val rand = MersenneTwister()
    val gp = Gnuplot()

    constructor() {
        positionIndex = Array(GRIDSIZESQ) {ArrayList<Agent>()}
        gp("set linetype 1 lc 'red'")
        gp("set linetype 2 lc 'blue'")
    }

    constructor(nPredator: Int, nPrey: Int) : this() {
        for(i in 1..nPredator)  add(Predator(rand.nextInt(GRIDSIZE), rand.nextInt(GRIDSIZE)))
        for(i in 1..nPrey)      add(Prey(rand.nextInt(GRIDSIZE), rand.nextInt(GRIDSIZE)))
    }

    constructor(lambdaPred: Double, lambdaPrey: Double) : this() {
        val nPredator = rand.nextPoisson(lambdaPred * GRIDSIZESQ)
        val nPrey = rand.nextPoisson(lambdaPrey * GRIDSIZESQ)
        for(i in 1..nPredator)  add(Predator(rand.nextInt(GRIDSIZE), rand.nextInt(GRIDSIZE)))
        for(i in 1..nPrey)      add(Prey(rand.nextInt(GRIDSIZE), rand.nextInt(GRIDSIZE)))
    }


    fun schedule(event: Event) = eventQueue.add(event)

    fun schedule(time: Double, agent: Agent): Event {
        val event = Event(time, agent)
        eventQueue.add(event)
        return event
    }

    fun remove(event: Event) {
        eventQueue.remove(event)
//        event.agent.nextEvent = null
    }


    fun nextEvent() = eventQueue.pollFirst()

    fun agentsAt(i: Int, j: Int) = agentsAt((i+GRIDSIZE).rem(GRIDSIZE)+(j+GRIDSIZE).rem(GRIDSIZE)*GRIDSIZE)

    fun agentsAt(id: Int) = positionIndex[id]

    fun add(agent: Agent) {
        agentsAt(agent.id).add(agent)
        agent.scheduleNextEvent(this)
        reSchedule(agent.sphereOfInfluence())
    }

    fun remove(agent : Agent) {
        var removedOK = agentsAt(agent.id).remove(agent)
        if(!removedOK) println("problems removing")
        agent.nextEvent?.let {
            remove(it)
        }
        reSchedule(agent.sphereOfInfluence())
    }

    fun reSchedule(sphereOfInfluence: List<Int>) {
        sphereOfInfluence.forEach { influencedPosition ->
            agentsAt(influencedPosition).forEach { influencedAgent ->
                influencedAgent.nextEvent?.let { remove(it) }
                influencedAgent.scheduleNextEvent(this)
            }
        }
    }

    fun plot() {
        val data = eventQueue.asSequence().flatMap { (_, agent) ->
            sequenceOf(agent.xPos, agent.yPos, if(agent is Prey) 1 else 2) }.toList()

//        val name = gp.heredoc(data, 3, data.size/3)
//        gp("plot [0:${GRIDSIZE}][0:${GRIDSIZE}] ${name} with points pointtype 5 pointsize 0.5 lc variable")
        gp("plot [0:${GRIDSIZE}][0:${GRIDSIZE}] '-' binary record=(${data.size/3}) using 1:2:3 with points pointtype 5 pointsize 0.5 lc variable")
        gp.write(data)

    }

    fun isConsistent(): Boolean {
        var consistent = true
//        positionIndex.forEach { (pos, agents) ->
//            agents.forEach { agent ->
//                if(agent.id != pos) println("position index is inconsistent!")
//            }
//        }

        eventQueue.forEach { event ->
            if(positionIndex[event.agent.id]?.contains(event.agent) != true) println("agent in event queue is not in position index")
            if(event.agent.nextEvent !== event) println("agent nextEvent is not consistent")
        }
        return consistent
    }

    fun averageMultiplicity(): Double {
        return positionIndex.asSequence().flatMap { agents ->
            val preds= agents.asSequence().count { it is Predator }
            val prey = agents.asSequence().count { it is Prey }
            sequenceOf(preds, prey)
        }.filter {it != 0}.average()
    }


    fun multiplicity(): Int {
        return positionIndex.asSequence().map { agents ->
            val preds= agents.asSequence().count { it is Predator }
            val prey = agents.asSequence().count { it is Prey }
            max(preds,prey)
        }.max()?:0
    }

    fun predatorMultiplicity(): Int {
        return positionIndex.asSequence().map { agents ->
            agents.asSequence().count { it is Predator }
        }.max()?:0
    }

    fun preyMultiplicity(): Int {
        return positionIndex.asSequence().map { agents ->
            agents.asSequence().count { it is Prey }
        }.max()?:0
    }


    fun nAgents() = eventQueue.size

    fun simulate(T: Double) {
        val endTime = time + T
        while(time < endTime) {
            val event = nextEvent()?:return
            if(event.time < endTime) {
                time = event.time
                event.agent.executeEvent(this)
            } else {
                time = endTime
                eventQueue.add(event)
            }
        }
    }

    // returns a map of agent to numberObserved
    fun observe(pObserve: Double) : Observation {
        val agents = Observation()
        eventQueue.forEach { (_,agent) ->
            agents.real.merge(agent, 1,Int::plus)
            if(rand.nextDouble() < pObserve) agents.observed.merge(agent, 1,Int::plus)
        }
        return agents
    }

    // returns a list of observations.
    // Each member is a map from agent to number observed
    fun generateObservations(pObserve: Double, nObservations: Int, observationInterval: Double): List<Observation> {
        val observations = ArrayList<Observation>()
        for(i in 1..nObservations) {
            simulate(observationInterval)
            observations.add(observe(pObserve))
        }
        return observations
    }
}