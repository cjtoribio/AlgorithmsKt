class LinearSieve(val L: Int) {
    val primes = ArrayList<Int>()
    val primeFactor = IntArray(L+1)
    init {
        for (i in 2 until primeFactor.size) {
            if (primeFactor[i] == 0) {
                primeFactor[i] = i
                primes.add(i)
            }
            for (j in 0 until primes.size) {
                if (primes[j] > primeFactor[i] || i * primes[j] > L) break
                primeFactor[i * primes[j]] = primes[j]
            }
        }
    }
    val retArr = IntArray(1_000)
    var retArrSize = 0
    fun factors(n: Int) : IntArray {
        retArrSize = 0
        var n = n
        while (n > 1) {
            retArr[retArrSize++] = primeFactor[n]
            n /= primeFactor[n]
        }
        return IntArray(retArrSize) { retArr[it] }
    }
    fun rec(V: IntArray, tp: Int, id: Int, c: Int) {
        if (id == V.size) {
            retArr[retArrSize++] = c
        } else {
            rec(V, 0, id+1, c)
            if (tp == 1 || V[id] != V[id-1])
                rec(V, 1, id+1, c * V[id])
        }
    }
    fun divisors(sorted: Boolean = false, n: Int) : IntArray {
        val f = factors(n)
        retArrSize = 0
        rec(f, 1, 0, 1)
        retArr.sort(0, retArrSize)
        return IntArray(retArrSize) { retArr[it] }
    }
}
