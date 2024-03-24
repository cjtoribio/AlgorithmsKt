class FreqArr(val N: Int, val bitsPerCell: Int = 64, val mod: Int = (1 shl bitsPerCell) - 1) {
    val A = LongArray(N / bitsPerCell + 1 )
    val ACU= IntArray( N / bitsPerCell + 2 )
    operator fun set(i : Int, v: Boolean) {
        val cell = i / bitsPerCell
        val subCell = i - cell * bitsPerCell
        if (v)
            A[cell] = A[cell] or (1L shl subCell)
        else
            A[cell] = A[cell] and (1L shl subCell).inv()
        ACU[cell+1] = A[cell].countOneBits() + ACU[cell]
    }
    operator fun get(i : Int): Boolean {
        val cell = i / bitsPerCell
        return ((A[cell] ushr (i / bitsPerCell)) and 1).toInt() == 1
    }
    fun getAcu(i: Int) : Int {
        val cell = i / bitsPerCell
        val subCell = i - cell * bitsPerCell
        return ACU[cell] + (A[cell] and ((1L shl (subCell + 1)) - 1)).countOneBits()
    }
}

class WaveletTree(arr: IntArray,
                  val b: Int = 0,
                  val e: Int = arr.size - 1,
                  val lo: Int = arr.min(),
                  val hi: Int = arr.max(),
                  val A: FreqArr = FreqArr(21 * arr.size),
                  offset: AtomicInteger = AtomicInteger(0)
) {
    var left: WaveletTree? = null
    var right: WaveletTree? = null
    val Aoffset: Int = offset.get()
    init {
        offset.set(Aoffset + e - b + 1)
        val m = (lo + hi) / 2
        // partition i..j
        var idx = b
        for (i in b..e) {
            if (arr[i] <= m) {
                A[Aoffset + i-b] = true
                arr[idx] = arr[i].also { arr[i] = arr[idx] }
                idx++
            } else {
                A[Aoffset + i-b] = false
            }
        }
        if (lo != hi &&  b != e) {
            left = WaveletTree(arr, b, idx-1, lo, m, A, offset)
            right = WaveletTree(arr, idx, e, m+1, hi, A, offset)
        }
    }

    fun getFreq(i: Int, j: Int, a: Int, b: Int): Int { // count elements from a to b
        if (j < i) return 0
        if (a <= lo && hi <= b) return j - i + 1
        if (b < lo || hi < a) return 0
        val ai: Int = A.getAcu(Aoffset + i - 1)
        val aj: Int = A.getAcu(Aoffset + j)
        return (left?.getFreq(ai, aj - 1, a, b) ?: 0) + (right?.getFreq(i - ai, j - aj, a, b) ?: 0)
    }
}
