package deselby.probabilisticABM

open class Act<AGENT>(val rate : Double, val subjectSelector : (AGENT) -> Boolean) {
}