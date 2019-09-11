package experiments.spatialPredatorPrey.generatorPoly

import deselby.distributions.FockState

class PredPreyBehaviour : Behaviour<PredPreyAgent>(arrayOf(
        object : Action<PredPreyAgent>(0.1) {
            override fun subjectSelector(subj: PredPreyAgent) = subj.isPredator

            override fun <FOCKSTATE : FockState<PredPreyAgent, FOCKSTATE>> invoke(subj: PredPreyAgent, pabm: FOCKSTATE): FOCKSTATE {
                return pabm.create(subj)
            }
        },

        object : Interaction<PredPreyAgent>(0.2) {
            override fun objectSelector(subj: PredPreyAgent) = sequenceOf(
                    PredPreyAgent(false, subj.iPos + 1, subj.jPos)
            )

            override fun <FOCKSTATE : FockState<PredPreyAgent, FOCKSTATE>> invoke(subj: PredPreyAgent, obj: PredPreyAgent, pabm: FOCKSTATE): FOCKSTATE {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun subjectSelector(subj: PredPreyAgent) = subj.isPredator

        }

))