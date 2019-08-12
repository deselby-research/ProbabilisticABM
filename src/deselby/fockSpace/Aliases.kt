package deselby.fockSpace

import deselby.std.vectorSpace.CovariantDoubleVector
import deselby.std.vectorSpace.DoubleVector
import deselby.std.vectorSpace.HashDoubleVector
import deselby.std.vectorSpace.MutableDoubleVector

typealias CreationVector<AGENT> = DoubleVector<CreationBasis<AGENT>>
typealias MutableCreationVector<AGENT> = MutableDoubleVector<CreationBasis<AGENT>>
typealias FockVector<AGENT> = DoubleVector<Basis<AGENT>>
typealias MutableFockVector<AGENT> = MutableDoubleVector<Basis<AGENT>>
typealias CovariantFockVector<AGENT> = CovariantDoubleVector<Basis<AGENT>>
typealias HashFockVector<AGENT> = HashDoubleVector<Basis<AGENT>>
typealias HashCreationVector<AGENT> = HashDoubleVector<CreationBasis<AGENT>>

typealias CommutationMap<AGENT> = Map<AGENT,FockVector<AGENT>>

typealias AnnihilationIndex<AGENT> = Map<AGENT, List<Map.Entry<Basis<AGENT>,Double>>>
