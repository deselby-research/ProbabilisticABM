package deselby.std.extensions

import org.apache.commons.math3.util.CombinatoricsUtils

// calculates the falling factorial (this)_delta
fun Int.fallingFactorial(delta: Int): Int {
    if(delta > this) return 0
    var f = 1
    for(i in this until this-delta) {
        f *= i
    }
    return f
}