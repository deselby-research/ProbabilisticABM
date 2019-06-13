package deselby.std

import kotlin.math.PI
import kotlin.math.sin

object Fourier {
    // NDArray should have shape (2^n,2) final dimension is for
    // real and imaginary components.
    // Forward transform is defined as
    //
    // H_n = \sum_{k=0}^{N-1} h_k e^{2\pi i k n / N}
    //
    // Backward is defined as
    //
    // h_k = 1/N \sum_{n=0}^{N-1} H_n e^{-2\pi i k n / N}
    //
    // See Numerical Recipes 3rd Edition Section 12.1
    fun transformInPlace(data : DoubleNDArray, isForward : Boolean) {
        if(data.shape[1] != 2) throw(IllegalArgumentException("second dimension should be of size 2 to store complex numbers"))
        if(data.shape[0] and (data.shape[0]-1) != 0) throw(IllegalArgumentException("size of list must be a power of 2"))
        if(data.shape[0] < 2) return
        val isign = if(isForward) 1 else -1
        var j = 0
        for(i in 0 until data.shape[0]) {
            if(j > i) {
                val tmp0 = data[i,0]
                val tmp1 = data[i,1]
                data[i,0] = data[j,0]
                data[i,1] = data[j,1]
                data[j,0] = tmp0
                data[j,1] = tmp1
            }
            var m = data.shape[0]/2
            while(m in 1 .. j) {
                j -= m
                m /= 2
            }
            j += m
        }
        var mmax = 1
        var wr : Double
        var wi : Double
        while(mmax < data.shape[0]) {
            val istep = mmax * 2
            val theta = isign*(PI /mmax)
            val wtemp = sin(0.5*theta)
            val wpr = -2.0*wtemp*wtemp
            val wpi = sin(theta)
            wr = 1.0
            wi = 0.0
            for(m in 0 until mmax) {
                for(i in m until data.shape[0] step istep) {
                    j = i + mmax
                    val tmpr = wr*data[j,0] - wi*data[j,1]
                    val tmpi = wr*data[j,1] + wi*data[j,0]
                    data[j,0] = data[i,0]-tmpr
                    data[j,1] = data[i,1]-tmpi
                    data[i,0] += tmpr
                    data[i,1] += tmpi
                }
                val wr0 = wr
                wr += wr * wpr - wi * wpi
                wi += wi * wpr + wr0 * wpi
            }
            mmax = istep
        }
        if(!isForward) {
            val s = 1.0/data.shape[0]
            for(i in 0 until data.shape[0]) {
                data[i,0] *= s
                data[i,1] *= s
            }
        }
    }
}

// NDArray should have shape (2^i0...2^in,2)
fun DoubleNDArray.fourierTransformInPlace(isForward : Boolean) {
    val slice = NDRange(shape.size) {0..0}
    slice[shape.size-1] = 0..1
    var i : Int
    var notOverflow : Boolean
    for(transformDimension in 0 .. shape.size-2) {
        slice[transformDimension] = 0 until shape[transformDimension]
        notOverflow = true
        while(notOverflow) {
            val oneDslice = this[slice].reShape(shape[transformDimension],2)
            Fourier.transformInPlace(oneDslice,isForward)
            // count through other dimensions
            i = shape.size - 2
            if(i == transformDimension) --i
            slice[i] = slice[i].first + 1 .. slice[i].last + 1
            notOverflow = true
            while (notOverflow && (slice[i].last == shape[i])) {
                slice[i] = 0..0
                if(--i == transformDimension) --i
                if(i<0) {
                    notOverflow = false
                } else {
                    slice[i] = slice[i].first + 1 .. slice[i].last + 1
                }
            }

        }
        slice[transformDimension] = 0..0
    }
}
