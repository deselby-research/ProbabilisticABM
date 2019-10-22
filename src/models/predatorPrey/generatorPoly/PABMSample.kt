package models.predatorPrey.generatorPoly

import deselby.distributions.discrete.GeneratorPolynomial
import deselby.std.collections.HashMultiset
import java.util.*
import kotlin.math.ln

class PABMSample<AGENT, ABM : MutableCollection<AGENT>>(initABMState : ABM) {

    val acts = ArrayList<ActRecord<AGENT>>()
    val agents = GeneratorPolynomial(initABMState)
    val rand = Random()

//    val size = agents.size
//    fun iterator() = agents.iterator()

//    override fun add(a: AGENT): Boolean {
//        if(agents.add(a)) {
//            addAgentsToActRecords(a, 1)
//            return true
//        }
//        return false
//    }
//
//    override fun remove(a : AGENT) : Boolean {
//        if(agents.remove(a)) {
//            for (act in acts) {
//                act.removeSubject(a, 1)
//                if (act.act is Interaction<AGENT>) act.removeObject(a, 1)
//            }
//            return true
//        }
//        return false
//    }

    fun integrate(t: Double): PABMSample<AGENT,ABM> {
        var remainingTime = t

        var totalActRate = acts.sumByDouble { it.rate }
        remainingTime += ln(1.0 - rand.nextDouble()) / totalActRate // negative coeff
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
            if (chosenAct is Action<AGENT>) {
                chosenAct(chosenActors.first, agents)
            }
            if (chosenAct is Interaction<AGENT>) {
                chosenAct(chosenActors.first, chosenActors.second, agents)
            }

            totalActRate = acts.sumByDouble { it.rate } // TODO: Make this perturbative rather than recalculating
            remainingTime += ln(1.0 - rand.nextDouble()) / totalActRate // negative coeff
        }

        return this
    }

    fun setBehaviour(b: Behaviour<AGENT>) {
        acts.clear()
        for(act in b.acts) {
            acts.add(ActRecord(act))
        }
        for(agent in agents.coeffs[0].abmState) { // assumes there's only one basis vector
            TODO("Calculate act rates by calculating the GroundedVector rate-of-change. This can be made " +
                    "efficient by calculating the perturbation to the rate-of-change when updating the d")
            addAgentsToActRecords(agent, 1)
        }
    }

    fun addAgentsToActRecords(agent : AGENT, n : Int) {
        for(actRecord in acts) {
            if(actRecord.act.subjectSelector(agent)) {
                if(actRecord.act is Action<AGENT>) actRecord.addActors(agent, agent, n)
                if(actRecord.act is Interaction<AGENT>) {
                    val objectAgents = actRecord.act.objectSelector(agent)
                    objectAgents.forEach { objectAgent ->
                        actRecord.addActors(agent, objectAgent, n)
                    }
                }
            }
        }
    }

    // Which agents apply to which acts
    class ActRecord<A>(val act : Act<A>, val actors : HashMultiset<Pair<A, A>>, var rate : Double) {
        constructor(act : Act<A>) : this(act, HashMultiset<Pair<A, A>>(), 0.0)

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