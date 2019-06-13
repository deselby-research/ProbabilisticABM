import deselby.std.DoubleNDArray
import deselby.std.fourierTransformInPlace


class MyClass(var i : Int, var j : Int) {
    override fun equals(other : Any?) : Boolean {
        if(other is MyClass) {
            if(super.equals(other)) {
                return true
            }
            return other.i == i && other.j == j
        }
        return false
    }


}

fun main() {
    val a = ArrayList<MyClass>()

    val x = MyClass(1,2)
    val y = MyClass(1,2)
    val z = MyClass(2,2)


    a.add(x)
    if(a.contains(x)) println("contains x")
    if(a.contains(y)) println("contains y")
    if(a.contains(z)) println("contains z")
    if(a.find {it.equals(x)} != null) println("found x")
    if(a.find {it.equals(y)} != null) println("found y")
    if(a.find {it.equals(z)} != null) println("found z")
}
