package deselby.fockSpace

import deselby.std.vectorSpace.AbstractDoubleVector
import deselby.std.vectorSpace.AbstractMutableDoubleVector

fun <AGENT, BASIS : FockElement<AGENT,BASIS>> AbstractDoubleVector<BASIS>.create(d : AGENT) : AbstractMutableDoubleVector<BASIS> {
    val result = zero()
    coeffs.mapKeysTo(result.coeffs) { it.key.create(d) }
    return result
}

operator fun <AGENT, OTHERBASIS : FockElement<AGENT,OTHERBASIS>>
        AbstractDoubleVector<OperatorBasis<AGENT>>.times(other : AbstractDoubleVector<OTHERBASIS>) {
//       : AbstractMutableDoubleVector<OTHERBASIS> {
//    var runningResult = other
}