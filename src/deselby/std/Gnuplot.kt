package deselby.std

import org.apache.commons.exec.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class Gnuplot {
    val execResult = DefaultExecuteResultHandler()
    val pipe : PipedOutputStream // pipe feeding the executable's standard in
    private val nativeBuffer : ByteBuffer = ByteBuffer.allocate(4)

    constructor(persist : Boolean = true, pipeOutputTo : OutputStream = System.out, pipeErrTo : OutputStream = System.err) {
        nativeBuffer.order(ByteOrder.nativeOrder())
        val execIn = PipedInputStream()
        val exec = DefaultExecutor()
        pipe = PipedOutputStream(execIn) // pipe feeding the executable's standard in
        val handler = PumpStreamHandler(pipeOutputTo, pipeErrTo, execIn)
        exec.streamHandler = handler
        val cl = CommandLine("gnuplot")
        if(persist) cl.addArgument("-p")
        exec.execute(cl, execResult)
    }

    // default plotters
    fun plot(data : Sequence<Float>, nRecords : Int=-1, ranges : String = "", plotStyle : String = "with lines title 'Kotlin data'", inferXCoord : Boolean = false) {
        val dataType = if(inferXCoord) "array" else "record"
        write("plot $ranges '-' binary $dataType=($nRecords) $plotStyle\n")
        write(data)
    }

    // data in order (x0,y0), (x0,y1)...(x0,yn), (x1,y0)...
    fun splot(data : Sequence<Float>, xSize : Int = -1, ySize : Int, ranges : String = "", plotStyle : String = "with lines title 'Kotlin data'", inferXYCoords : Boolean = false) {
        val dataType = if(inferXYCoords) "array=($xSize,$ySize) transpose" else "record=($ySize,$xSize)"
        write("splot $ranges '-' binary $dataType $plotStyle\n")
        write(data)
    }

    // define a here-document
    // N.B. this will only work with Gnuplot 5.0 upwards
    fun define(name : String, data : Sequence<Float>, nFields : Int, nRecords : Int = -1, nBlocks : Int = -1, nFrames : Int = -1) {
        val dataIt = data.iterator()
        write("\$$name << EOD\n")
        var frame = nFrames
        do {
            var block = nBlocks
            do {
                var record = nRecords
                do {
                    for(f in 1 until nFields) {
                        if(!dataIt.hasNext()) throw(IllegalArgumentException("not enough data points"))
                        write(dataIt.next().toString())
                        write(" ")
                    }
                    write(dataIt.next().toString())
                    write("\n")
                } while(if(nRecords ==-1) dataIt.hasNext() else --record !=0)
                write("\n")
            } while(if(nBlocks == -1) dataIt.hasNext() else --block != 0)
            write("\n")
        } while(if(nFrames == -1) dataIt.hasNext() else --frame != 0)
        write("EOD\n")
        if(dataIt.hasNext()) throw(IllegalArgumentException("too many data points"))
    }

    fun undefine(name : String) = write("undefine \$$name\n")

    class XYIterator(xSize: Int, ySize: Int) : Iterator<XYIterator> {
        val x : Float
            get() = xi.toFloat()
        val y : Float
            get() = yi.toFloat()
        var xi = 0
            private set
        var yi = -1
            private set
        var xSize : Int = xSize
            private set
        var ySize : Int = ySize
            private set

        override fun hasNext() = ((xi < xSize-1) || (yi < ySize-1))
        override fun next(): XYIterator {
            ++yi
            if(yi == ySize) {
                yi = 0
                ++xi
            }
            if(xi == xSize) {
                xi = 0
            }
            return this
        }
    }

    fun generateXYSequence(xSize : Int, ySize : Int) :Sequence<XYIterator> {
        return Sequence { XYIterator(xSize, ySize) }
    }

    fun generateXSequence(xSize : Int) :Sequence<Int> {
        return (0..xSize).asSequence()
    }

    fun write(data : Sequence<Float>) {
        data.forEach {
            nativeBuffer.putFloat(0, it)
            pipe.write(nativeBuffer.array())
        }
    }

    fun write(s: String) = pipe.write(s.toByteArray())

    fun write(f: Float) {
        nativeBuffer.putFloat(0, f)
        pipe.write(nativeBuffer.array())
    }

    fun close() = pipe.close()

    // Use this to force gnuplot to plot without having to close the connection
    // e.g. to do animation
    fun flush() {
        for(i in 1..250) {
            write("# fill gnuplots buffer with comments\n") // this persuades gnuplot to read its input!
        }
        pipe.flush()
    }

    // invoke gnuplot command
    operator fun invoke(s : String) {
        pipe.write(s.toByteArray())
        pipe.write('\n'.toInt())
    }

    // wait for termination of binary
    fun waitFor() = execResult.waitFor()
    fun waitFor(timeout : Long) = execResult.waitFor(timeout)

}
