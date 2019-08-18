import deselby.fockSpace.BinomialLikelihood
import deselby.std.Gnuplot
import deselby.std.gnuplot
import java.util.*


fun main() {
    for(i in (0..10)) {
        println("---- $i ----")
        when {
            i < 3 -> println("is < 3")
            i < 1 -> println("is < 1")
            i < 5 -> println("is < 5")
        }
    }
}
