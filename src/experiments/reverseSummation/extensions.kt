package experiments.reverseSummation

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.extensions.times
import kotlin.math.exp


// Calculates sum this * e^HT * startState using reverse
// matrix exponential integration
// 'tolerance' gives the absolute value below which terms in the taylor expansion
// will be truncated
fun<AGENT> FockVector<AGENT>.reverseIntegrateAndSum(hIndex: CreationIndex<AGENT>, T: Double, startState: Ground<AGENT>, tolerance: Double): Double {
    var taylorTerm = this * exp(-T)
    var expansionSum = (taylorTerm * startState).values.sum()
//    val poissonMax = hamiltonian.normLinfty() * T
    var lastTermSum = 0.0
    var termSum = tolerance
    var order = 1
    while(termSum > tolerance || termSum > lastTermSum) {
        lastTermSum = termSum
        taylorTerm = (T/order)*(taylorTerm + taylorTerm.semiCommuteAndStrip(hIndex))
        termSum = (taylorTerm.timesAndMarginalise(startState, emptySet())).values.sum()
        expansionSum += termSum
//        println("iteration $order termSize = ${taylorTerm.size} termSum = $termSum expansionSum = $expansionSum")
        ++order
    }
    return expansionSum
}


fun<AGENT> FockVector<AGENT>.reverseIntegrateAndSum(hIndex: CreationIndex<AGENT>, T: Double, startState: Ground<AGENT>, expansionOrder: Int): Double {
    var taylorTerm = this * exp(-T)
    var expansionSum = (taylorTerm * startState).values.sum()
    var termSum: Double
    for(order in 1..expansionOrder) {
        taylorTerm = (T/order)*(taylorTerm + taylorTerm.semiCommuteAndStrip(hIndex))
        termSum = (taylorTerm.timesAndMarginalise(startState, emptySet())).values.sum()
        expansionSum += termSum
//        println("iteration $order termSize = ${taylorTerm.size} termSum = $termSum expansionSum = $expansionSum")
    }
    return expansionSum
}


// Calculates the expectation value of this * e^HT * startState given observations
// using reverse matrix exponential integration
fun<AGENT> FockVector<AGENT>.reversePosteriorMean(hcIndex: CreationIndex<AGENT>,
                                                  haIndex: AnnihilationIndex<AGENT>,
                                                  H: FockVector<AGENT>,
                                                  T: Double,
                                                  startState: GroundedBasis<AGENT, DeselbyGround<AGENT>>,
                                                  observations: BinomialBasis<AGENT>, expansionOrder: Int): Double
{
    val observationFootprint = this.toAnnihhilationFootprint().observationFootprint(hcIndex, haIndex, expansionOrder)
    val observedAgents = observations.observations.keys.intersect(observationFootprint)
    val Lg = LgOperator(observedAgents, observations.pObserve)
    val LgCommuteH = Lg.commute(H)
    val kLgStartState = GroundedBasis(startState.basis, Lg * startState.ground) // not normalised but don't care
    val nonZeroObservations = observedAgents
            .filter { observations.observations[it] != 0 }
            .associateWith {observations.observations.getValue(it)}
    val binomialBasis = Basis.newBasis(nonZeroObservations, nonZeroObservations)

//    println("Calculating posterior of $this with observation ${observedAgents.map {AbstractMap.SimpleEntry(it, observations.observations[it])}}")
    // Do integration
    var taylorTerm = (this * binomialBasis).stripCreations() * exp(-T)
    var jointSum = (taylorTerm * kLgStartState).values.sum()
    var termSum = 0.0
    for(order in 1..expansionOrder) {
        taylorTerm = (T/order)*(taylorTerm + taylorTerm.semiCommuteAndStrip(hcIndex) + taylorTerm.multiplyAndStrip(LgCommuteH))
        termSum = (taylorTerm.timesAndMarginalise(kLgStartState, emptySet())).values.sum()
        jointSum += termSum
//        println("iteration $order termSize = ${taylorTerm.size} termSum = $termSum expansionSum = $jointSum")
    }
    println("integrated joint up to term size $termSum")
    if(observedAgents.isEmpty()) return jointSum

    // calculate normalisation
    taylorTerm = binomialBasis.toVector().stripCreations() * exp(-T)
    var normalisationSum = (taylorTerm * kLgStartState).values.sum()
    for(order in 1..expansionOrder) {
        taylorTerm = (T/order)*(taylorTerm + taylorTerm.semiCommuteAndStrip(hcIndex) + taylorTerm.multiplyAndStrip(LgCommuteH))
        termSum = (taylorTerm.timesAndMarginalise(kLgStartState, emptySet())).values.sum()
        normalisationSum += termSum
//        println("iteration $order termSize = ${taylorTerm.size} termSum = $termSum expansionSum = $normalisationSum")
    }
    println("integrated normalisation up to term size $termSum")
//    println("posterior = ${jointSum / normalisationSum}")
    return jointSum / normalisationSum
}


fun<AGENT> FockVector<AGENT>.toAnnihhilationFootprint(): Set<AGENT> {
    val presentAgents = HashSet<AGENT>()
    this.forEach { (termBasis,_) ->
        termBasis.annihilations.forEach { (agent, _) ->
            presentAgents.add(agent)
        }
    }
    return presentAgents
}


fun<AGENT> Set<AGENT>.observationFootprint(hcIndex: CreationIndex<AGENT>, haIndex: AnnihilationIndex<AGENT>, order: Int): Set<AGENT> {
    return this.reverseFootprint(hcIndex, order).forwardFootprint(haIndex, order)
}

fun<AGENT> Set<AGENT>.reverseFootprint(hIndex: CreationIndex<AGENT>, order: Int): Set<AGENT> {
    var activeAgents = this
    for(i in 1..order) {
        val nextOrderActiveAgents = HashSet<AGENT>()
        activeAgents.forEach { currentActiveAgent ->
            hIndex[currentActiveAgent]?.forEach { (hamiltonianTerm, _) ->
                hamiltonianTerm.annihilations.forEach { (termAnnihilationAgent, _) ->
                    nextOrderActiveAgents.add(termAnnihilationAgent)
                }
            }
        }
        activeAgents = nextOrderActiveAgents
    }
    return activeAgents
}


fun<AGENT> Set<AGENT>.forwardFootprint(hIndex: AnnihilationIndex<AGENT>, order: Int): Set<AGENT> {
    var activeAgents = this
    for(i in 1..order) {
        val nextOrderActiveAgents = HashSet<AGENT>()
        activeAgents.forEach { currentActiveAgent ->
            hIndex[currentActiveAgent]?.forEach { (hamiltonianTerm, _) ->
                hamiltonianTerm.creations.forEach { (termCreationAgent, _) ->
                    nextOrderActiveAgents.add(termCreationAgent)
                }
            }
        }
        activeAgents = nextOrderActiveAgents
    }
    return activeAgents
}

// list of footprints for all orders up to maxOrder
// the n'th item in the list is the n'th order foortprint
fun<AGENT> Set<AGENT>.forwardFootprintList(hIndex: AnnihilationIndex<AGENT>, maxOrder: Int): ArrayList<Set<AGENT>> {
    val allFootprints = ArrayList<Set<AGENT>>(maxOrder)
    allFootprints.add(this)
    for(i in 1..maxOrder) {
        val nextOrderActiveAgents = HashSet<AGENT>()
        allFootprints.last().forEach { currentActiveAgent ->
            hIndex[currentActiveAgent]?.forEach { (hamiltonianTerm, _) ->
                hamiltonianTerm.creations.forEach { (termCreationAgent, _) ->
                    nextOrderActiveAgents.add(termCreationAgent)
                }
            }
        }
        allFootprints.add(nextOrderActiveAgents)
    }
    return allFootprints
}

