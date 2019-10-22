package experiments.reverseSummation

import deselby.fockSpace.FockVector

open class ReverseIntegralCache {
    private val cache = HashMap<Any, TaylorTerms<*>>()

//    @Synchronized
//    operator fun<AGENT> set(observable: FockVector<AGENT>, integral: List<FockVector<AGENT>>) {
//        cache[observable] = integral
//    }

    @Synchronized
    operator fun<AGENT> get(observable: FockVector<AGENT>): TaylorTerms<AGENT> {
        return cache.getOrPut(observable) { TaylorTerms<AGENT>() } as TaylorTerms<AGENT>
    }

    class TaylorTerms<AGENT>(val terms: ArrayList<FockVector<AGENT>> = ArrayList()) {
        @Synchronized
        operator fun get(index: Int) = terms.get(index)

        @Synchronized
        fun add(term: FockVector<AGENT>) = terms.add(term)

        val size: Int
            @Synchronized
            get() = terms.size
    }
}

object LikelihoodCache: ReverseIntegralCache()

object NormalisationCache: ReverseIntegralCache()

