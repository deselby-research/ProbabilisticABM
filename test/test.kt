import java.util.*

class MyClass(val c: Int) {
    override fun hashCode(): Int {
        return c
    }

    override fun equals(other: Any?): Boolean {
        if(other !is MyClass) return false
        return c == other.c
    }

    override fun toString(): String {
        return "c = $c"
    }
}

fun main(args : Array<String>) {
    val m = HashMap<Any,Any>()

    m[9] = 1234
    m["hello"] = "goodbye"
    m[MyClass(1234)] = MyClass(2345)

    println(m["hello"])
    println(m[9])
    println(m[1.0])
    println(m[MyClass(1234)])
}
