package models

import deselby.fockSpace.FockVector
import deselby.fockSpace.HashFockVector

class RandomWalk(val size: Int): FockModel<Int> {
    override fun calcFullHamiltonian(): FockVector<Int> {
        val H = HashFockVector<Int>()
        for(i in 0 until size) {
            H += FockAgent.action(i, 0.5, (i+1).rem(size))
            H += FockAgent.action(i, 0.5, (i+size-1).rem(size))
        }
        return H
    }
}