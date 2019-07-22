package experiments.fockBasis

import deselby.std.collections.HashMultiset
import deselby.std.collections.hashMultisetOf

object Operators {
    fun <AGENT> identity() = OneHotFock<AGENT>(OperatorBasis(),1.0)
    fun <AGENT> create(d: AGENT) = OneHotFock(OperatorBasis(hashMapOf(d to 1), HashMap()),1.0)
    fun <AGENT> annihilate(d: AGENT) = OneHotFock(OperatorBasis(HashMap(), hashMapOf(d to 1)),1.0)
}