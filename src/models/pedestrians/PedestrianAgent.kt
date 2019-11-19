package models.pedestrians

import deselby.fockSpace.HashFockVector
import models.FockAgent

class PedestrianAgent(val pos: Int, val leftHanded: Boolean) {

    fun hamiltonian(H: HashFockVector<PedestrianAgent>, params: Params) {
        H += FockAgent.action(this, params.rUp, PedestrianAgent(up(pos), leftHanded))
        val horizontalPos = if(leftHanded) left(pos) else right(pos)
        H += FockAgent.action(this, params.rHorizontal, PedestrianAgent(horizontalPos, leftHanded))
    }

    fun right(size: Int) = pos - pos.rem(size) + (pos+1).rem(size)
    fun left(size: Int) = pos - pos.rem(size) + (pos+size-1).rem(size)
    fun up(size: Int) = (pos + size).rem(size*size)
//    fun down(size: Int) = (pos + size*size - size).rem(size*size)

    override fun hashCode() = pos*2 + if(leftHanded) 1 else 0

    override fun equals(other: Any?) = ((other is PedestrianAgent) && pos == other.pos && leftHanded == other.leftHanded)

}
