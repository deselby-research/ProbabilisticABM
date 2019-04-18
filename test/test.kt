import deselby.std.nextPoisson
import org.apache.commons.math3.random.MersenneTwister
import kotlin.math.exp
import kotlin.system.measureTimeMillis

interface MyInterface {

}

class MyClass1 : MyInterface {
    fun doAThing() {}
}

class MyClass2 : MyInterface {
    fun doAnotherThing() {}
}

operator fun <T : MyInterface> T.times(x : T) : T {
    return x
}

fun main(args : Array<String>) {
    var myObj1 = MyClass1()
    var myObj2 = MyClass2()

    (myObj1 * myObj1).doAThing()

    (myObj2 * myObj2).doAnotherThing()

//    println(measureTimeMillis {
//
//    })
}
