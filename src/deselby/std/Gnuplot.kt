package deselby.std

import org.apache.commons.exec.*
import java.io.*
import java.lang.Thread.sleep
import java.nio.ByteBuffer
import java.nio.ByteOrder
///////////////////////////////
// use this to plot data to Gnuplot. Use like this:
// gnuplot {
//    invoke("plot x*x")
// }
// Data can be sent in two ways. Either as a heredoc:
// gnuplot {
//    val data = heredoc(data,2)
//    invoke("""
//       plot $data with lines
//       ...other commands...
//    """)
// }
// ...or in binary format...
// gnuplot {
//    invoke("""
//       plot ${binary(2,100)} with lines
//       ...other commands...
//    """)
//    write(data)
// }
/////////////////////////////
open class Gnuplot : Closeable {
    val execResult = DefaultExecuteResultHandler()
    val pipe : PipedOutputStream // pipe feeding the executable's standard in
    private val nativeBuffer : ByteBuffer = ByteBuffer.allocate(4)
    var nextDataId = 1

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
        sleep(128) // wait for gnuplot instance to spin-up
    }


    // This is a helper for sending data to gnuplot in binary form.
    // Piping data over in binary is quicker, if speed is important
    // To send data in binsry format use the syntax
    //    invoke("plot '-' binary record=(nRecords) using 1:2 with lines")
    //    write(data)
    // or
    //    invoke("splot '-' binary record=(recordsPerBlock, nBlocks) using 1:2:3 with lines")
    //    write(data)
    //
    // replot doesn't work with binary data. Instead do something like:
    //    invoke("plot '-' binary record=(100) using 1 with lines, '-' binary record=(80) using 1:2 with points")
    //    write(data1)
    //    write(data2)
    // use this function as a helper inside string literals. E.g. the above invoke could be written
    //    invoke("splot ${binary(1,100)} with lines, ${binary(2,80)} with points")
    //
    fun binary(fieldsPerRecord: Int, nRecords: Int=-1) : String {
        val using = (2..fieldsPerRecord).fold("1") {s,i -> "$s:$i"}
        return "'-' binary record=($nRecords) using $using"
    }

    fun binary(fieldsPerRecord: Int, recordsPerBlock: Int, nBlocks: Int=-1) : String {
        val using = (2..fieldsPerRecord).fold("1") {s,i -> "$s:$i"}
        return "'-' binary record=($recordsPerBlock,$nBlocks) using $using"
    }

    fun heredoc(data : Iterable<Triple<Number,Number,Number>>, recordsPerBlock: Int) =
            heredoc(data.asSequence().flatMap { sequenceOf(it.first, it.second, it.third) }, 3, recordsPerBlock)

    fun heredoc(data : Sequence<Triple<Number,Number,Number>>, recordsPerBlock: Int) =
            heredoc(data.flatMap { sequenceOf(it.first, it.second, it.third) }, 3, recordsPerBlock)

    fun heredoc(data : Iterable<Pair<Number,Number>>) =
            heredoc(data.asSequence().flatMap { sequenceOf(it.first, it.second) }, 2)

    fun heredoc(data : Sequence<Pair<Number,Number>>) =
            heredoc(data.flatMap { sequenceOf(it.first, it.second) }, 2)

    fun heredoc(data : Iterable<Number>, fieldsPerRecord : Int = 1, recordsPerBlock : Int = -1, blocksPerFrame : Int = -1, nFrames : Int = -1) =
            heredoc(data.asSequence(), fieldsPerRecord, recordsPerBlock, blocksPerFrame, nFrames)

    fun heredoc(data : Sequence<Number>, fieldsPerRecord : Int = 1, recordsPerBlock : Int = -1, blocksPerFrame : Int = -1, nFrames : Int = -1): String {
        val name = getUniqueDataName()
        define(name, data, fieldsPerRecord, recordsPerBlock, blocksPerFrame, nFrames)
        return "\$$name"
    }

    // define a here-document
    // N.B. this will only work with Gnuplot 5.0 upwards
    fun define(name : String, data : Sequence<Number>, fieldsPerRecord : Int = 1, recordsPerBlock : Int = -1, blocksPerFrame : Int = -1, nFrames : Int = -1) {
        val dataIt = data.iterator()
        write("\$$name << EOD\n")
        var frame = nFrames
        do {
            var block = blocksPerFrame
            do {
                var record = recordsPerBlock
                do {
                    for(f in 1 until fieldsPerRecord) {
                        if(!dataIt.hasNext()) throw(IllegalArgumentException("not enough data points"))
                        write(dataIt.next().toString())
                        write(" ")
                    }
                    write(dataIt.next().toString())
                    write("\n")
                } while(if(recordsPerBlock ==-1) dataIt.hasNext() else --record !=0)
                write("\n")
            } while(if(blocksPerFrame == -1) dataIt.hasNext() else --block != 0)
            write("\n")
        } while(if(nFrames == -1) dataIt.hasNext() else --frame != 0)
        write("EOD\n")
        if(dataIt.hasNext()) throw(IllegalArgumentException("too many data points"))
    }

    fun undefine(name : String) {
        write("undefine \$$name\n")
    }

    fun write(data: Iterable<Number>) { rawWrite(data.asSequence().map(Number::toFloat)) }

    fun write(data: Sequence<Number>) { rawWrite(data.map(Number::toFloat)) }

    fun rawWrite(data : Iterable<Float>) { rawWrite(data.asSequence()) }

    fun rawWrite(data : Sequence<Float>) {
        data.forEach {
            nativeBuffer.putFloat(0, it)
            pipe.write(nativeBuffer.array())
        }
    }

    fun write(s: String) { pipe.write(s.toByteArray()) }

    override fun close() { pipe.close() }

    // Use this to force gnuplot to plot without having to close the connection
    // e.g. to do animation
    fun flush(): Gnuplot {
        for(i in 1..250) {
            write("# fill gnuplots buffer with comments\n") // this persuades gnuplot to read its input!
        }
        pipe.flush()
        return this
    }

    // invoke gnuplot command
    operator fun invoke(command : String) {
        val str = command.trimIndent()
        pipe.write(str.toByteArray())
        pipe.write('\n'.toInt())
    }


    fun getUniqueDataName() : String {
        return "data${nextDataId++}"
    }

    // wait for termination of binary
    fun waitFor() = execResult.waitFor()
    fun waitFor(timeout : Long) = execResult.waitFor(timeout)

    companion object {
        data class XYCoord(val x: Int, val y: Int)
        fun generateXYSequence(xSize : Int, ySize : Int) =
                (0 until xSize*ySize).asSequence().map { XYCoord(it.div(ySize), it.rem(ySize)) }

        fun generateXSequence(xSize : Int) = (0 until xSize).asSequence()
    }
}

fun<R> gnuplot(persist : Boolean = true, pipeOutputTo : OutputStream = System.out, pipeErrTo : OutputStream = System.err, command: Gnuplot.() -> R): R {
    return Gnuplot(persist, pipeOutputTo, pipeErrTo).use {
        it.run(command)
    }
}
