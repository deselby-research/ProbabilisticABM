package deselby.probabilisticABM

import deselby.std.HashMultiset
import java.util.*
import kotlin.math.ln

class PABMSample<AGENT> : PABM<AGENT> {

    // Which agents apply to which acts
    class ActRecord<A>(val act : Act<A>, val subjects : HashMultiset<A>, val objects : HashMultiset<A>, var rate : Double) {
        constructor(act : Act<A>) : this(act, HashMultiset<A>(), HashMultiset<A>(), 0.0)

        fun addSubject(agent : A, n : Int) {
            subjects.add(agent, n)
            recalcRate()
        }


        fun removeSubject(agent : A, n : Int) : Boolean {
            val success = subjects.remove(agent, n)
            recalcRate()
            return success
        }

        fun addObject(agent : A, n : Int) {
            objects.add(agent, n)
            recalcRate()
        }


        fun removeObject(agent : A, n : Int) : Boolean {
            val success = objects.remove(agent,n)
            recalcRate()
            return success
        }

        fun recalcRate() {
            if(act is Action)
                rate = act.rate * subjects.size
            else
                rate = act.rate * subjects.size * objects.size
        }
    }

    val acts = ArrayList<ActRecord<AGENT>>()
    val agents = HashMultiset<AGENT>()
    val rand = Random()

    override fun add(a: AGENT): PABM<AGENT> {
        return add(a,1)
    }

    override fun add(a: AGENT, n: Int): PABM<AGENT> {
        agents.add(a,n)
        addAgentsToActRecords(a,n)
        return this
    }

    private fun remove(a : AGENT) : PABM<AGENT> {
        agents.remove(a)
        for(act in acts) {
            act.removeSubject(a,1)
            if(act.act is Interaction) act.removeObject(a,1)
        }
        return this
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
            val chosenSubject = chosenRecord.subjects.elementAt(rand.nextInt(chosenRecord.subjects.size))
            if (chosenAct is Action) {
                this.remove(chosenSubject)
                chosenAct.op(chosenSubject, this)
            }
            if (chosenAct is Interaction) {
                val chosenObject = chosenRecord.objects.elementAt(rand.nextInt(chosenRecord.objects.size))
                this.remove(chosenSubject)
                this.remove(chosenObject)
                chosenAct.op(chosenSubject, chosenObject, this)
            }

            totalActRate = acts.sumByDouble { it.rate }
            remainingTime += ln(1.0 - rand.nextDouble()) / totalActRate // negative value
        }

        return this
    }

    override fun setBehaviour(b: Behaviour<AGENT>) {
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
            if(actRecord.act.subjectSelector(agent)) actRecord.addSubject(agent, n)
            if(actRecord.act is Interaction && actRecord.act.objectSelector(agent)) {
                actRecord.addObject(agent, n)
            }
        }
    }
}