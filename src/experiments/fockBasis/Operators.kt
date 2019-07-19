package experiments.fockBasis

import deselby.std.collections.HashMultiset
import deselby.std.collections.hashMultisetOf

object Operators {
    fun <AGENT> identity() = OneHotFock<AGENT>(OperatorBasis(),1.0)
    fun <AGENT> create(d: AGENT) = OneHotFock(OperatorBasis(hashMultisetOf(d), HashMultiset()),1.0)
    fun <AGENT> annihilate(d: AGENT) = OneHotFock(OperatorBasis(HashMultiset(), hashMultisetOf(d)),1.0)
}