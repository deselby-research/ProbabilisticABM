package deselby

import java.util.*
import kotlin.collections.HashSet
import kotlin.math.ln

class PABMSample<AGENT> : PABM<AGENT> {
    class ActRecord<A>(val act :Act<A>, val subjects : HashSet<A>, val objects : HashSet<A>, var rate : Double) {
        constructor(act : Act<A>) : this(act, HashSet<A>(), HashSet<A>(), 0.0)

        fun addSubject(agent : A) : Boolean {
            if(subjects.add(agent)) {
                recalcRate()
                return true
            }
            return false
        }

        fun removeSubject(agent : A) : Boolean {
            if(subjects.remove(agent)) {
                recalcRate()
                return true
            }
            return false
        }

        fun addObject(agent : A) : Boolean {
            if(objects.add(agent)) {
                recalcRate()
                return true
            }
            return false
        }

        fun removeObject(agent : A) : Boolean {
            if(objects.remove(agent)) {
                recalcRate()
                return true
            }
            return false
        }

        fun recalcRate() {
            if(act is Action)
                rate = act.rate * subjects.size
            else
                rate = act.rate * subjects.size * objects.size
        }
    }

    val acts = ArrayList<ActRecord<AGENT>>()
    val agents = HashSet<AGENT>()
    val rand = Random()

    override fun add(a: AGENT): PABM<AGENT> {
        agents.add(a)
        addAgentToActRecords(a)
        return this
    }

    private fun remove(a : AGENT) : PABM<AGENT> {
        agents.remove(a)
        for(act in acts) {
            act.removeSubject(a)
            if(act.act is Interaction) act.removeObject(a)
        }
        return this
    }

    override fun integrate(T: Double): PABM<AGENT> {
        var remainingTime = T

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
        for(agent in agents) {
            addAgentToActRecords(agent)
        }
    }

    fun addAgentToActRecords(agent : AGENT) {
        for(actRecord in acts) {
            if(actRecord.act.subjectSelector(agent)) actRecord.addSubject(agent)
            if(actRecord.act is Interaction && actRecord.act.objectSelector(agent)) {
                actRecord.addObject(agent)
            }
        }
    }
}