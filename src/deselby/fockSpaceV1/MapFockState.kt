package deselby.fockSpaceV1

interface MapFockState<AGENT> : FockState<AGENT, MapFockState<AGENT>> {
    val coeffs : Map<FockBasis<AGENT>, Double>

    operator fun get(b : FockBasis<AGENT>) : Double = coeffs[b]?:0.0
}
