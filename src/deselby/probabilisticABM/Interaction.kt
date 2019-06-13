package deselby.probabilisticABM


class Interaction<AGENT>(
        rate : Double,
        subjectSelector : (AGENT) -> Boolean,
        val objectSelector : (AGENT) -> Boolean,
        val op : (AGENT, AGENT, PABM<AGENT>) -> PABM<AGENT>
) : Act<AGENT>(rate, subjectSelector)
