package experiments.reverseSummation

import deselby.fockSpace.*
import deselby.fockSpace.extensions.*
import deselby.std.vectorSpace.extensions.times
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt


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
        println("iteration $order termSize = ${taylorTerm.size} termSum = $termSum expansionSum = $expansionSum")
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
    val startFootprint = this.toAnnihhilationFootprint().reverseFootprint(hcIndex, expansionOrder)
//    val forwardFootprints = startFootprint.forwardFootprintList(haIndex, expansionOrder)
    val observationFootprint = startFootprint //.forwardFootprint(haIndex, expansionOrder)
    val observedAgents = observations.observations.keys.intersect(observationFootprint)
    val Lg = LgOperator(observedAgents, observations.pObserve)
    val LgCommuteH = Lg.commute(H)
    val kLgStartState = GroundedBasis(startState.basis, Lg * startState.ground) // not normalised but don't care
    val nonZeroObservations = observedAgents
            .filter { observations.observations[it] != 0 }
            .associateWith {observations.observations.getValue(it)}
    val binomialBasis = Basis.newBasis(nonZeroObservations, nonZeroObservations)

//    println("Calculating posterior of $this with observation ${observedAgents.map {AbstractMap.SimpleEntry(it, observations.observations[it])}}")
    val jointSum = (this * binomialBasis).reverseBinomialIntegrateAndSum(hcIndex, LgCommuteH, T, kLgStartState, expansionOrder)
    if(observedAgents.isEmpty()) return jointSum

//    val marginalisedH = H.reverseMarginalise(observationFootprint.reverseFootprint(hcIndex, expansionOrder))
//    val HLgD0 = ((Basis.identityVector<AGENT>() + marginalisedH + Lg.commute(marginalisedH)) * kLgStartState.basis * kLgStartState.ground).asGroundedVector(kLgStartState.ground)
//    val nextOrderTerm = (T/(expansionOrder+1)) * taylorTerm.timesAndMarginalise(HLgD0, emptySet()).creationVector
//    println("next order term sum = ${nextOrderTerm.values.sum()}")

    val normalisationSum = binomialBasis.toVector().reverseBinomialIntegrateAndSum(hcIndex, LgCommuteH, T, kLgStartState, expansionOrder)
//    println("posterior = ${jointSum / normalisationSum}")
    return jointSum / normalisationSum
}

fun<AGENT> FockVector<AGENT>.reverseBinomialIntegrateAndSum(hcIndex: CreationIndex<AGENT>, LgCommuteH: FockVector<AGENT>, T: Double, ground: GroundedBasis<AGENT,DeselbyGround<AGENT>>, expansionOrder: Int): Double {
    val taylorTerm = HashFockVector(this.stripCreations())
    val amplitude = (taylorTerm * ground).values.sum()
    var weight = exp(-T)
    var jointSum = weight * amplitude
    var termSum = 0.0
//    var xtestimate = 0.0
//    var err = 0.0
    for(order in 1..expansionOrder) {
        weight *= T/order
        val tCommuteH = taylorTerm.semiCommuteAndStrip(hcIndex)
        val tLgCommuteH = taylorTerm.multiplyAndStrip(LgCommuteH)
        taylorTerm += tCommuteH
        taylorTerm += tLgCommuteH
        termSum = weight * (taylorTerm.timesAndMarginalise(ground, emptySet())).values.sum()
        jointSum += termSum

//        println("termSize = ${taylorTerm.size} termSum = ${100.0*termSum/jointSum}% expansionSum = $jointSum")

        // remove small terms (worst-case assume smallest n terms go to 1)
//        if(order < expansionOrder) {
//            taylorTerm.entries.removeIf { (basis, weight) ->
//                val probs = ArrayList<Double>()
//                basis.annihilations.forEach { (d, n) ->
//                    val p = ground.ground.lambda(d)
//                    for(i in ground.basis[d] until n) {
//                        probs.add(p)
//                    }
//                }
//                probs.sortDescending()
//                probs.dropLast(expansionOrder - order)
//                (probs.fold(1.0, Double::times) * weight).absoluteValue < 1e-6
//            }
//        }
//        println("truncated termSize = ${taylorTerm.size}")
//        taylorTerm.printOrderHistogram()

        // estimate truncation error
//        xtestimate += (termSum.absoluteValue/(weight*amplitude)).pow(1.0/order) * T
//        val xt = xtestimate/order
//        err = amplitude*(exp(xt) - 1.0)
//        var w = amplitude
//        for(i in 1..order) {
//            w *= 1.0/i
//            err -= w*xt.pow(i)
//        }
//        println("estimated truncation error = ${100.0*exp(-T)*err / jointSum}%")

    }
//    println("estimated truncation error = ${100.0*exp(-T)*err / jointSum}%")
//    println()
    return jointSum
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
        val nextOrderActiveAgents = HashSet<AGENT>(activeAgents)
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
        val nextOrderActiveAgents = HashSet<AGENT>(activeAgents)
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
    val allFootprints = ArrayList<Set<AGENT>>(maxOrder+1)
    allFootprints.add(this)
    for(i in 1..maxOrder) {
        val nextOrderActiveAgents = HashSet<AGENT>(allFootprints.last())
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


// prints histogram of the order of the terms in this (i.e. total number of operators in a term)
// between 0th and 10th order
fun<AGENT> FockVector<AGENT>.printOrderHistogram() {
    val termCounts = Array(11) { 0 }
    this.keys.forEach {basis ->
        val basisOrder = basis.creations.values.sum() + basis.annihilations.values.sum()
        termCounts[basisOrder.coerceIn(0..10)] += 1
    }
    for(bin in 0..10) {
        print("$bin : ")
        val stars = (termCounts[bin]*40.0/this.size).roundToInt()
        (1..stars).forEach {print('*')}
        println()
    }
}

//fun<AGENT> FockVector<AGENT>.hamiltonianMarginalise(annihilationAgents: Set<AGENT>, creationAgents: Set<AGENT>): FockVector<AGENT> {
//    val marginalisation = HashFockVector<AGENT>()
//    this.forEach {(basis, weight) ->
//        if(annihilationAgents.containsAll(basis.annihilations.keys)) {
//            marginalisation.plusAssign(
//                    Basis.newBasis(
//                            basis.creations.filter {(d,_) -> creationAgents.contains(d)},
//                            basis.annihilations
//                    ),
//                    weight,
//                    1e-14
//            )
//        }
//    }
//    return marginalisation
//}
//
