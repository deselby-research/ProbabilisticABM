package experiments.SpatialPredatorPrey.generatorPoly

class Action<ABM : Collection<AGENT>, AGENT>(
        rate : Double,
        selector : (AGENT) -> Boolean,
        val op : (AGENT, ABM) -> AGENT)
    : Act<ABM, AGENT>(rate, selector) { }