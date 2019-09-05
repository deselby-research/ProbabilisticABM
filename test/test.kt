import deselby.fockSpace.BinomialLikelihood
import deselby.std.Gnuplot
import deselby.std.gnuplot
import deselby.std.step
import java.util.*
import kotlin.math.floor


fun main() {
    for(i in -5.0 .. 5.0 step 0.125) {
        println("$i ${i - floor(i)}")
    }
}
