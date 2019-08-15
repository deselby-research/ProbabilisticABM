package deselby.std

import org.apache.commons.exec.*
import java.io.*
import java.lang.Thread.sleep
import java.nio.ByteBuffer
import java.nio.ByteOrder

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


    // This form sends data in binary format
    // piping data over in binary is quicker, if speed is important
    // replot doesn't work with binary data. Instead do something like:
    //     invoke("plot '-' binary record=(100) using 1 with lines, '-' binary record=(80) using 1:2 with points")
    //     write(data1)
    //     write(data2)
    fun plotBinary(data : Sequence<Number>,
                   fieldsPerRecord: Int, recordsPerBlock: Int, ranges: String = "", plotStyle: String = "with lines"): Gnuplot {
        val using = (2..fieldsPerRecord).fold("1") {s,i -> "$s:$i"}
        write("plot $ranges '-' binary record=($recordsPerBlock) using $using $plotStyle\n")
        rawWrite(data.map {it.toFloat()})
        return this
    }


    // This form sends data in binary format
    // piping data over in binary is quicker, if speed is important
    // data in order (x0,y0), (x0,y1)...(x0,yn), (x1,y0)...
    // replot doesn't work with binary data. Instead do something like:
    //     invoke("splot '-' binary record=(100,100) using 1 with lines, '-' binary record=(80,50) using 1:2:3 with points")
    //     write(data1)
    //     write(data2)
    fun splotBinary(data : Sequence<Number>,
                    fieldsPerRecord: Int, xSize: Int, ySize: Int, ranges : String = "", plotStyle : String = "with lines"): Gnuplot {
        val using = (2..fieldsPerRecord).fold("1") {s,i -> "$s:$i"}
        write("splot $ranges '-' binary record=($ySize,$xSize) using $using $plotStyle\n")
        rawWrite(data.map(Number::toFloat))
        return this
    }

    fun plot(yData: Iterable<Number>, fieldsPerRecord :Int=1, recordsPerBlock: Int=-1, ranges: String="", style: String="with lines") =
            plot(yData.asSequence(), fieldsPerRecord, recordsPerBlock, ranges, style)

    fun plot(yData: Sequence<Number>, fieldsPerRecord: Int=1, recordsPerBlock: Int=-1, ranges: String="", style: String="with lines"): Gnuplot {
        val dataId = getUniqueDataName()
        define(dataId, yData, fieldsPerRecord, recordsPerBlock)
        write("plot $ranges \$$dataId $style\n")
        return(this)
    }

    fun plot(xyData: Iterable<Pair<Number,Number>>, ranges: String="", style: String="with lines") =
            plot(xyData.asSequence(), ranges, style)

    fun plot(xyData: Sequence<Pair<Number,Number>>, ranges: String="", style: String="with lines") =
            plot(xyData.flatMap { sequenceOf(it.first, it.second) },2,-1, ranges, style)


    fun replot(yData: Iterable<Number>, fieldsPerRecord :Int=1, recordsPerBlock: Int=-1, style: String="with lines") =
            replot(yData.asSequence(), fieldsPerRecord, recordsPerBlock)

    fun replot(yData: Sequence<Number>, fieldsPerRecord :Int=1, recordsPerBlock: Int=-1, style: String="with lines"): Gnuplot {
        val dataId = getUniqueDataName()
        define(dataId, yData, fieldsPerRecord, recordsPerBlock)
        write("replot \$$dataId $style\n")
        return(this)
    }

    fun replot(xyData: Iterable<Pair<Number,Number>>, style: String="with lines") =
            replot(xyData.asSequence(), style)

    fun replot(xyData: Sequence<Pair<Number,Number>>, style: String="with lines") =
            replot(xyData.flatMap { sequenceOf(it.first, it.second) }, 2, -1, style)

    fun replot(xyzData: Iterable<Triple<Number,Number,Number>>, recordsPerBlock: Int, style: String="with lines") =
            replot(xyzData.asSequence(), recordsPerBlock, style)

    fun replot(xyzData: Sequence<Triple<Number,Number,Number>>, recordsPerBlock: Int, style: String="with lines") =
            replot(xyzData.flatMap {sequenceOf(it.first, it.second, it.third)}, 3, recordsPerBlock, style)


    fun splot(xyzData: Iterable<Number>, fieldsPerRecord: Int=1, recordsPerBlock: Int, ranges: String="", style: String="with lines") =
        splot(xyzData.asSequence(), fieldsPerRecord, recordsPerBlock, ranges, style)

    fun splot(xyzData: Sequence<Number>, fieldsPerRecord: Int=1, recordsPerBlock: Int, ranges: String="", style: String="with lines") : Gnuplot {
        val dataId = getUniqueDataName()
        define(dataId, xyzData, fieldsPerRecord, recordsPerBlock)
        write("splot $ranges \$$dataId $style\n")
        return(this)

    }

    fun splot(xyzData: Iterable<Triple<Number,Number,Number>>, recordsPerBlock: Int, ranges: String="", style: String="with lines") =
            splot(xyzData.asSequence(), recordsPerBlock, ranges, style)

    fun splot(xyzData: Sequence<Triple<Number,Number,Number>>, recordsPerBlock: Int, ranges: String="", style: String="with lines") =
            splot(xyzData.asSequence().flatMap { sequenceOf(it.first, it.second, it.third) }, 3, recordsPerBlock, ranges, style)


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

    fun heredoc(data : Iterable<Pair<Number,Number>>) =
            heredoc(data.asSequence().flatMap { sequenceOf(it.first, it.second) }, 2)

    fun heredoc(data : Sequence<Pair<Number,Number>>) =
            heredoc(data.flatMap { sequenceOf(it.first, it.second) }, 2)

    fun heredoc(data : Iterable<Triple<Number,Number,Number>>, recordsPerBlock: Int) =
            heredoc(data.asSequence().flatMap { sequenceOf(it.first, it.second) }, 3, recordsPerBlock)

    fun heredoc(data : Sequence<Triple<Number,Number,Number>>, recordsPerBlock: Int) =
            heredoc(data.flatMap { sequenceOf(it.first, it.second) }, 3, recordsPerBlock)

    fun heredoc(data : Iterable<Number>, fieldsPerRecord : Int = 1, recordsPerBlock : Int = -1, blocksPerFrame : Int = -1, nFrames : Int = -1) =
            heredoc(data.asSequence(), fieldsPerRecord, recordsPerBlock, blocksPerFrame, nFrames)

    fun heredoc(data : Iterable<Iterable<Number>>, recordsPerBlock : Int = -1, blocksPerFrame : Int = -1, nFrames : Int = -1): String {
        val fieldsPerRecord = data.iterator().next().count()
        return heredoc(data.asSequence().flatMap { it.asSequence() }, fieldsPerRecord, recordsPerBlock, blocksPerFrame, nFrames)
    }

    fun heredoc(data : Sequence<Number>, fieldsPerRecord : Int = 1, recordsPerBlock : Int = -1, blocksPerFrame : Int = -1, nFrames : Int = -1): String {
        val name = getUniqueDataName()
        define(name, data, fieldsPerRecord, recordsPerBlock, blocksPerFrame, nFrames)
        return "\$$name"
    }

    // define a here-document
    // N.B. this will only work with Gnuplot 5.0 upwards
    fun define(name : String, data : Sequence<Number>, fieldsPerRecord : Int = 1, recordsPerBlock : Int = -1, blocksPerFrame : Int = -1, nFrames : Int = -1): Gnuplot {
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
        return this
    }

    fun undefine(name : String): Gnuplot {
        write("undefine \$$name\n")
        return this
    }

    fun write(data: Iterable<Number>) = rawWrite(data.asSequence().map(Number::toFloat))

    fun write(data: Sequence<Number>) = rawWrite(data.map(Number::toFloat))

    fun rawWrite(data : Iterable<Float>) = rawWrite(data.asSequence())

    fun rawWrite(data : Sequence<Float>) : Gnuplot {
        data.forEach {
            nativeBuffer.putFloat(0, it)
            pipe.write(nativeBuffer.array())
        }
        return this
    }

    fun write(s: String) = pipe.write(s.toByteArray())


    override fun close() = pipe.close()

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
    operator fun invoke(command : String): Gnuplot {
        val str = command.trimIndent()
        pipe.write(str.toByteArray())
        pipe.write('\n'.toInt())
        return this
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

fun<R> gnuplot(command: Gnuplot.() -> R): R {
    return Gnuplot().use {
        it.run(command)
    }
}

