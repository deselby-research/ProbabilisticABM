package models

import deselby.fockSpace.FockVector

interface FockModel<AGENT> {
    fun calcFullHamiltonian(): FockVector<AGENT>
}
