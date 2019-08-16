package experiments.SIR

// beta =  rate of infection per si pair
// gamma = rate of recovery per person
// lambdaS = Poisson lambda for susceptible at time t=0
// lambdaI = Poisson lambda for infected at time t=0
data class SIRParams(val beta: Double, val gamma: Double, val lambdaS: Double, val lambdaI: Double)