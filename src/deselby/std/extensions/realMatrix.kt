package deselby.std.extensions

import org.apache.commons.math3.linear.RealMatrix

inline operator fun RealMatrix.get(i: Int, j: Int) = this.getEntry(i,j)

inline operator fun RealMatrix.set(i: Int, j: Int, value: Double) = this.setEntry(i,j,value)

inline operator fun RealMatrix.times(other: RealMatrix): RealMatrix = this.multiply(other)

inline operator fun RealMatrix.times(multiplier: Double): RealMatrix = this.scalarMultiply(multiplier)

inline operator fun RealMatrix.div(multiplier: Double): RealMatrix = this.scalarMultiply(1.0/multiplier)

inline operator fun RealMatrix.minus(other: RealMatrix): RealMatrix = this.subtract(other)

inline operator fun RealMatrix.plus(other: RealMatrix): RealMatrix = this.add(other)

inline operator fun RealMatrix.minus(scalar: Double): RealMatrix = this.scalarAdd(-scalar)

inline operator fun RealMatrix.plus(scalar: Double): RealMatrix = this.scalarAdd(scalar)
