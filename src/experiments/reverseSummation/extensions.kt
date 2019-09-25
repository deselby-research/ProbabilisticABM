package experiments.reverseSummation

import deselby.fockSpace.*
import deselby.fockSpace.extensions.semiCommuteAndStrip
import deselby.fockSpace.extensions.times
import deselby.fockSpace.extensions.toCreationIndex
import deselby.std.vectorSpace.extensions.times
import kotlin.math.exp


// Calculates sum this * e^HT * startState using reverse
// matrix exponential integration
// 'tolerance' gives the absolute value below which terms in the taylor expansion
// will be truncated
fun<AGENT, GROUND: Ground<AGENT>> FockVector<AGENT>.reverseIntegrateAndSum(hamiltonian: FockVector<AGENT>, T: Double, startState: GroundedVector<AGENT,GROUND>, tolerance: Double): Double {

    var taylorTerm = this * exp(-T)
    var expansionSum = exp(-T)
    val hIndex = hamiltonian.toCreationIndex()
    val poissonMax = hamiltonian.normLinfty() * T
    var termSum = 1.0
    var order = 1
    while(termSum > tolerance || order < poissonMax) {
        taylorTerm = (T/order)*(taylorTerm + taylorTerm.semiCommuteAndStrip(hIndex))
        termSum = (taylorTerm * startState).creationVector.values.sum()
        expansionSum += termSum
        ++order
    }
    return expansionSum
}
