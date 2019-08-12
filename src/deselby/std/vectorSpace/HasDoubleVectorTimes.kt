package deselby.std.vectorSpace

interface HasDoubleVectorTimes<in RBASIS, OUTBASIS> {
    operator fun times(other: RBASIS): DoubleVector<OUTBASIS>
}
