package experiments.SpatialPredatorPrey.generatorPoly

import deselby.distributions.FockBasis
import deselby.std.HashMultiset
import java.util.*
import kotlin.math.ln

class PABMSample<AGENT> : PABM<AGENT>, AbstractMutableCollection<AGENT>() {

    val acts = ArrayList<ActRecord<AGENT>>()
    val agents = HashMultiset<AGENT>()
    val rand = Random()

    override val size = agents.size
    override fun iterator() = agents.iterator()

    override fun add(a: AGENT): Boolean {
        if(agents.add(a)) {
            addAgentsToActRecords(a, 1)
            return true
        }
        return false
    }

    override fun remove(a : AGENT) : Boolean {
        if(agents.remove(a)) {
            for (act in acts) {
                act.removeSubject(a, 1)
                if (act.act is Interaction<*,*>) act.removeObject(a, 1)
            }
            return true
        }
        return false
    }

    override fun integrate(t: Double): PABM<AGENT> {
        var remainingTime = t

        var totalActRate = acts.sumByDouble { it.rate }
        remainingTime += ln(1.0 - rand.nextDouble()) / totalActRate // negative value
        while(remainingTime > 0.0) {

            // choose next act
            var randomCumulativeRate = rand.nextDouble() * totalActRate
            val actIterator = acts.iterator()
            var chosenRecord: ActRecord<AGENT>
            do {
                chosenRecord = actIterator.next()
                randomCumulativeRate -= chosenRecord.rate
            } while (randomCumulativeRate > 0.0)


            val chosenAct = chosenRecord.act
            val chosenActors = chosenRecord.actors.elementAt(rand.nextInt(chosenRecord.actors.size))
            if (chosenAct is Action) {
                this.remove(chosenActors.first)
                chosenAct.op(chosenActors.first, this)
            }
            if (chosenAct is Interaction) {
                this.remove(chosenActors.first)
                this.remove(chosenActors.second)
                chosenAct.op(chosenActors.first, chosenActors.second, this)
            }

            totalActRate = acts.sumByDouble { it.rate } // TODO: Make this perturbative rather than recalculating
            remainingTime += ln(1.0 - rand.nextDouble()) / totalActRate // negative value
        }

        return this
    }

    override fun setBehaviour(b: Behaviour<PABM<AGENT>, AGENT>) {
        acts.clear()
        for(act in b.acts) {
            acts.add(ActRecord(act))
        }
        for(agent in agents.memberCountMapEntries()) {
            addAgentsToActRecords(agent.key, agent.value)
        }
    }

    fun addAgentsToActRecords(agent : AGENT, n : Int) {
        for(actRecord in acts) {
            if(actRecord.act.subjectSelector(agent)) {
                if(actRecord.act is Action<PABM<AGENT>,AGENT>) actRecord.addActors(agent, agent, n)
                if(actRecord.act is Interaction<PABM<AGENT>,AGENT>) {
                    val objectAgents = actRecord.act.objectSelector(agent, this)
                    objectAgents.forEach { objectAgent ->
                        actRecord.addActors(agent, objectAgent, n)
                    }
                }
            }
        }
    }

    // Which agents apply to which acts
    class ActRecord<A>(val act : Act<PABM<A>, A>, val actors : HashMultiset<Pair<A,A>>, var rate : Double) {
        constructor(act : Act<PABM<A>,A>) : this(act, HashMultiset<Pair<A,A>>(), 0.0)

        fun addActors(subjectActor : A, objectActor : A, n : Int) {
            actors.add(Pair(subjectActor, objectActor), n)
            recalcRate()
        }


        fun removeSubject(agent : A, n : Int) : Boolean {
            val success = actors.removeIf {(subjectActor, _) -> subjectActor == agent}
            recalcRate()
            return success
        }

        fun removeObject(agent : A, n : Int) : Boolean {
            val success = actors.removeIf {(_, objectActor) -> objectActor == agent}
            recalcRate()
            return success
        }

        private fun recalcRate() {
            rate = act.rate * actors.size
        }
    }

}