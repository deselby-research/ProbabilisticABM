package deselby.probabilisticABM


class Action<AGENT>(rate : Double, selector : (AGENT) -> Boolean, val op : (AGENT, PABM<AGENT>) -> PABM<AGENT>) : Act<AGENT>(rate, selector) {
}