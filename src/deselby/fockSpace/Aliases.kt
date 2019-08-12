package deselby.fockSpace

import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.MutableDoubleVector

typealias ActCreationVector<AGENT> = DoubleVector<ActCreationBasis<AGENT>>
typealias MutableCreationVector<AGENT> = MutableDoubleVector<ActCreationBasis<AGENT>>
typealias ActVector<AGENT> = DoubleVector<ActBasis<AGENT>>
typealias MutableOperatorVector<AGENT> = MutableDoubleVector<ActBasis<AGENT>>
