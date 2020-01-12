package models.pedestrians

import deselby.fockSpace.FockVector
import deselby.fockSpace.HashFockVector
import models.FockModel

// This class is the Hamiltonian for a pedestrian
class FockPedestrians(val params: Params): FockModel<PedestrianAgent> {
    override fun calcFullHamiltonian(): FockVector<PedestrianAgent> {
        val H = HashFockVector<PedestrianAgent>()
        for (pos in 0 until params.GRIDSIZESQ) {
            PedestrianAgent(pos, true).hamiltonian(H, params)
            PedestrianAgent(pos, false).hamiltonian(H, params)
        }
        return H

    }
}