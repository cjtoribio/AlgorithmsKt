class Fenwick(val N: Int) {
    val A = LongArray(N+1)
    fun add(i: Int, v: Long) {
        var x = i + 1
        while (x < A.size) {
            A[x] += v
            x += x and -x
        }
    }
    fun get(i: Int) : Long {
        var x = i + 1; var ret = 0L
        while (x > 0) {
            ret += A[x]
            x -= x and -x
        }
        return ret
    }

    override fun toString(): String {
        return (0 until A.size - 1).map { get(it) }.joinToString(" ", prefix = "[", postfix = "]")
    }
}
