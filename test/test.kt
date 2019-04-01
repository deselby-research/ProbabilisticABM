
fun main(args : Array<String>) {
    for(i in -20..20) {
        val x = 0.1*i
        println("$x ${x.rem(1.0)}")
    }

}
