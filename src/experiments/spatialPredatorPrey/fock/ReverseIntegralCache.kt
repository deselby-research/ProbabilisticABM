package experiments.spatialPredatorPrey.fock

import deselby.fockSpace.FockVector

open class ReverseIntegralCache {
    private val cache = HashMap<Any,MutableList<*>>()

//    @Synchronized
//    operator fun<AGENT> set(observable: FockVector<AGENT>, integral: List<FockVector<AGENT>>) {
//        cache[observable] = integral
//    }

    @Synchronized
    operator fun<AGENT> get(observable: FockVector<AGENT>): MutableList<FockVector<AGENT>> {
        return cache.getOrPut(observable) {ArrayList<FockVector<AGENT>>()} as MutableList<FockVector<AGENT>>
    }
}

object LikelihoodCache: ReverseIntegralCache()

object NormalisationCache: ReverseIntegralCache()

