import deselby.fockSpace.BinomialLikelihood
import deselby.std.Gnuplot
import deselby.std.gnuplot

fun main() {
    val data = Gnuplot.generateXYSequence(6,6).map {
        BinomialLikelihood.basisNormalisation(it.x,it.y,0.01)
    }

    val data1 = (1..200).asSequence().map {
        BinomialLikelihood.basisNormalisation(it,it,0.1)
    }

    val lambda = (1..100).asSequence().map {
        BinomialLikelihood.basisNormalisation(5,5,it*0.001)
    }

    println(lambda.toList())

    gnuplot {
        val doc = heredoc(data,1,6,6)
//        val doc1 = heredoc(data1,1,10)
        invoke("splot $doc with lines")
    }
    for(n in 0..10) {
        for(m in 0..10) {
            println("$n $m ${BinomialLikelihood.basisNormalisation(n,m,0.1)}")
        }
    }
}
