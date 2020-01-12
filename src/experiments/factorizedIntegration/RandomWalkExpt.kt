package experiments.factorizedIntegration

import deselby.fockSpace.*
import deselby.fockSpace.extensions.commute
import deselby.fockSpace.extensions.times
import models.FockAgent
import org.junit.Test

class RandomWalkExpt {

    @Test
    fun randomWalk() {
//        val H = models.RandomWalk(10).calcFullHamiltonian()
        val H = hamiltonianFermiDiracRandomWalk()
        val state5 = HashFockVector<Int>(CreationBasis(mapOf(5 to 1)) to 1.0)
        val state6 = HashFockVector<Int>(CreationBasis(mapOf(6 to 1)) to 1.0)
        val state56 = HashFockVector<Int>(CreationBasis(mapOf(5 to 1, 6 to 1)) to 1.0)
        println(H.commute(state5))
        println(H.commute(state6))
        println(H.commute(state56))
        println(state5*H.commute(state6) + H.commute(state5)*state6)
        println(H.commute(state6)*state5 + state6*H.commute(state5))
//        for(t in 1..4) {
//            state += H.commute(state)
//            println("${state.size}  $state")
//        }
    }

    @Test
    fun agentOperatorTest() {
        val rate = 1.0
//        val H = FockAgent.interaction(1,2,rate,1)
        val H = FockAgent.actionInAbsence(1,2,rate,2)

        val state1 = HashFockVector(CreationBasis(mapOf(1 to 1)) to 1.0)
        val state2 = HashFockVector(CreationBasis(mapOf(2 to 1)) to 1.0)
        val c1c2 = HashFockVector(CreationBasis(mapOf(1 to 1, 2 to 1)) to 1.0)
        val c1c2a1a2 = HashFockVector(InteractionBasis(mapOf(1 to 1, 2 to 1),1,2) to 1.0)
        val c1a2 = HashFockVector(ActionBasis(mapOf(1 to 1),2) to 1.0)
        val c1nota2 = state1 - HashFockVector(ActionBasis(mapOf(1 to 1, 2 to 1),2) to 1.0)
        val c1nota1 = state1 - HashFockVector(ActionBasis(mapOf(1 to 2),1) to 1.0)
        val H1 = c1nota2 + H.commute(c1nota2)
        val HH1 = H1 + H.commute(H1)
//        println("H = $H")
//        println("(I + [H,.])X = $H1")
//        println("(I + [H,.])a*(12) = ${state12 + H.commute(state12)}")
//        println("(I + [H,.])a*(1) = ${state1 + H.commute(state1)}")
//        println("(I + [H,.])a*(2) = ${state2 + H.commute(state2)}")
//        println("(I + [H,.])(${c1nota1}) = ${c1nota1 + H.commute(c1nota1)}")
//        println("(I + [H,.])(${H1}) = ${HH1}")

        println("(I - [${c1c2a1a2},.])$c1c2 = ${c1c2 - c1c2a1a2.commute(c1c2)}")

    }


    @Test
    fun actionCommutatorTest() {
        val rho = 1.0
        val interaction = FockAgent.interaction(1,2, rho, 3,4)
        val s1 = HashFockVector(CreationBasis(mapOf(1 to 1)) to 1.0)
        val c1 = s1*rho + interaction.commute(s1)
        println(c1)

        val rho2 = 1.0
        val action = FockAgent.action(2, rho2, 5)
        val c2 = c1*rho2 + action.commute(c1)
        println(c2)
    }


    @Test
    fun simpleTest() {
        val A = HashFockVector(InteractionBasis(emptyMap(),1,2) to 1.0)
        val B = HashFockVector(CreationBasis(mapOf(1 to 1, 2 to 1)) to 1.0)
        println(A.commute(B))
    }

    fun hamiltonianFermiDiracRandomWalk(): FockVector<Int> {
        val size = 10
        val H = HashFockVector<Int>()
        for(i in 0 until size) {
            H += FockAgent.actionInAbsence(i, (i+1).rem(size),  1.0, (i+1).rem(size))
//            H += FockAgent.action(i,  1.0, (i+1).rem(size))
//            H += FockAgent.actionInAbsence(i, (i+size-1).rem(size),0.5, (i+size-1).rem(size))
        }
        return H
    }

}