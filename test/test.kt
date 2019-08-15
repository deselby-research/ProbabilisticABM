import deselby.fockSpace.CommutationCoefficient
import deselby.std.Gnuplot
import deselby.std.gnuplot
import kotlin.math.cos
import kotlin.math.sin

fun main() {
//    val dataType = if(inferXYCoords) "array=($xSize,$ySize) transpose" else "record=($ySize,$xSize)"
//    write("splot $ranges '-' binary $dataType $plotStyle\n")

    val sindata = (0..100).asSequence().map{sin(it*0.1)}

    val data = Gnuplot.generateXYSequence(20,30).flatMap {
        sequenceOf( sin(it.x*0.1f)*sin(it.y*0.1f))
    }

    val data123 = Gnuplot.generateXYSequence(20,30).map {
        Triple(it.x, it.y, sin(it.x*0.1f)*sin(it.y*0.1f))
    }

    val data1 = (1..100).asSequence().map {Pair(it*0.1f,sin(it*0.1))}
    val data2 = (1..100).asSequence().flatMap {sequenceOf(cos(it*0.1f))}
    val data12 = (1..100).asSequence().flatMap {sequenceOf(it*0.1f, cos(it*0.1f))}

    gnuplot {
        val myVar = heredoc(data12, 2)
        val data2 = heredoc(data1)
        invoke("""
            plot ${binary(2,100)} with lines
        """)
        write(data12)
    }
//    val using = (2..10).fold("1") {s,i -> "$s:$i"}
//
//    val gp = Gnuplot().invoke("splot '-' binary format=\"%1float\" record=(30,20) with lines")
//    gp.write(data)
//    gp.close()
}
