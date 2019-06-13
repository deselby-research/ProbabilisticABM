package experiments.SpatialPredatorPrey.generatorPoly

class Interaction<ABM : Collection<AGENT>, AGENT>(
        rate : Double,
        subjectSelector : (AGENT) -> Boolean,
        val objectSelector : (AGENT,ABM) -> Sequence<AGENT>,
        val op : (AGENT, AGENT, ABM) -> AGENT
) : Act<ABM, AGENT>(rate, subjectSelector)
