package deselby

open class Act<AGENT>(val rate : Double, val subjectSelector : (AGENT) -> Boolean) {
}