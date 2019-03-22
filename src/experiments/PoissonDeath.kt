package experiments

import kotlin.math.ln
import kotlin.math.min

fun main(args : Array<String>) {
    val N = 40
    var a = DoubleArray(N, {0.0})
    a[0] = 1.0
    val lambda = 8.0
    val dt = 0.0001

    for(t in 0..20000) {
        a = DoubleArray(N, {i ->
            when (i) {
                0 -> a[0] + (lambda*a[0] + a[1])*dt
                N-1 -> a[i] + ((lambda-i)*a[i] - lambda*a[i-1])*dt
                else -> a[i] + ((lambda-i)*a[i] + (i+1)*a[i+1] - lambda*a[i-1])*dt

//                0 -> a[0] + (lambda*a[0] + a[1])*dt
//                N-1 -> a[i] + ((lambda-i)*a[i] - lambda*i*a[i-1])*dt
//                else -> a[i] + ((lambda-i)*a[i] + a[i+1] - lambda*i*a[i-1])*dt
            }
        })
        println("${a.asList().subList(0,min(20,N))}")
    }
}