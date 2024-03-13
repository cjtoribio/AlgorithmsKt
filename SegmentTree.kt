interface Value<A> {
    operator fun plus(other: A): A
}
interface Update<T, U: Update<T, U>> {
    operator fun plus(element: T) : T
    operator fun plus(element: U) : U
    operator fun times(other: Int) : U
}

class SegmentTree<T: Value<T>, U: Update<T, U>>(val D: ArrayList<U?>, val V: ArrayList<T>) {
    companion object {
        inline operator fun <reified T : Value<T>, reified U : Update<T, U>> invoke(init: Array<T>): SegmentTree<T, U> {
            val updateArray = ArrayList((0..<init.size).map { null as U? })
            return SegmentTree(updateArray, ArrayList((init + init).toList()))
        }
    }

    val N = D.size
    val H = Int.SIZE_BITS - N.countLeadingZeroBits()
    val pos = ArrayList(IntArray(60) { 1 }.toList())
    val SZ = ArrayList(IntArray(2 * N) { 1 }.toList())

    private fun Int.left(): Int = this.shl(1)
    private fun Int.right(): Int = this.shl(1).or(1)
    private fun Int.parent(): Int = this.shr(1)

    init {
        for (i in (N - 1) downTo 1) {
            SZ[i] = SZ[i.left()] + SZ[i.right()]
            V[i] = V[i.left()] + V[i.right()]
        }
    }

    fun apply(p: Int, u: U) {
        if (p < N) {
            D[p] = D[p]?.let { it + u } ?: u
            V[p] = (u * SZ[p]) + V[p]
        }
        else V[p] = (u * SZ[p]) + V[p]
    }

    fun calc(p: Int) {
        if (D[p] == null) V[p] = V[p.left()] + V[p.right()]
        else V[p] = (D[p]!! * SZ[p]) + (V[p.left()] + V[p.right()])
    }

    fun build(_l: Int, _r: Int) {
        var k = 2
        var l = _l + N
        var r = _r + N-1
        while (l > 1) {
            l = l shr 1
            r = r shr 1
            for (i in r downTo l) calc(i)
            k = k shl 1
        }
    }

    fun push(_l: Int, _r: Int) {
        var l = _l
        var r = _r
        var s = H
        var k = 1 shl (H - 1)
        l += N
        r += N-1
        while (s > 0) {
            for (i in (l shr s)..(r shr s)) if (D[i] != null) {
                apply(i.left(), D[i]!!)
                apply(i.right(), D[i]!!)
                D[i] = null
            }
            --s
            k = k shr 1
        }
    }

    fun pushToChildren(i: Int) {
        if (i < N) {
            D[i]?.let { apply(i.left(), it) }
            D[i]?.let { apply(i.right(), it) }
            D[i] = null
        }
    }

    fun update(_l: Int, _r: Int, u: U) {
        var r = min(_r + 1, N) + N
        var l = max(_l, 0) + N
        push(_l, _l + 1)
        push(_r, _r + 1)
        while (l < r) {
            if (l.and(1) != 0) apply(l++, u)
            if (r.and(1) != 0) apply(--r, u)
            l = l.parent()
            r = r.parent()
        }
        build(_l, _l + 1)
        build(_r, _r + 1)
    }

    fun query(_l: Int, _r: Int): T {  // sum on interval [l, r]
        var r = min(_r + 1, N) + N
        var l = max(_l, 0) + N
        var st = 0
        var en = 60
        push(_l, _l + 1)
        push(_r, _r + 1)
        while (l < r) {
            if (l.and(1) != 0) pos[st++] = l++
            if (r.and(1) != 0) pos[--en] = --r
            l = l.parent()
            r = r.parent()
        }
        return ((0..<st) + (en..<pos.size)).map { V[pos[it]] }.reduce { a,b -> a + b }
    }

    fun printTree(n: Int = 1, d : Int = 0, toString: (Int, v: T, u: U?) -> String = { i, v, _ -> "$v" }) : List<String> {
        if (n >= N) return listOf(toString(n, V[n], null))
        val l = printTree(n.left(), d + 1, toString).map { " " + it }
        val r = printTree(n.right(), d + 1, toString).map { it + " " }
        val szL = l[0].length
        val szR = r[0].length
        val szM = toString(n, V[n], D[n]).length
        return listOf(" ".repeat(szL) + toString(n, V[n], D[n]) + " ".repeat(szR)) +
                (l + (1..max(0, r.size - l.size)).map { " ".repeat(szR) }).zip(
                    (r + (1..max(0, l.size - r.size)).map { " ".repeat(szL) })
                ).map { (l, r) -> l + " ".repeat(szM) + r}
    }

    override fun toString(): String {
        return IntRange(0, N - 1).map { query(it, it) }.joinToString(" ")
    }

    fun segments(_l: Int, _r: Int): List<Int> {
        var r = min(_r + 1, N) + N
        var l = max(_l, 0) + N
        var st = 0
        var en = 60;
        push(_l, _l+1); if (l != r) push(_r, _r+1)
        while (l < r) {
            if (l.and(1) != 0) pos[st++] = l++
            if (r.and(1) != 0) pos[--en] = --r
            l = l.parent()
            r = r.parent()
        }
        return ((0..<st) + (en..<pos.size)).map { pos[it] }
    }

    fun findOkPrefix(_l: Int, deb: Boolean = false, isOk: (T) -> Boolean): Int {
        if (_l >= N) return 0
        var test: T? = null
        var sz = 0
        val goDown = fun(_i: Int) {
            var i = _i
            while (i < 2 * N) {
                pushToChildren(i)
                val ntest: T = if (test != null) test!! + V[i] else V[i]
                if (isOk(ntest)) {
                    test = ntest
                    sz += SZ[i]
                    i = i.or(1).left()
                } else i = i.left()
            }
        }
        for (s in segments(_l, N - 1)) {
            var ntest: T = if (test != null) test!! + V[s] else V[s]
            if (isOk(ntest)) {
                test = ntest
                sz += SZ[s]
            } else {
                goDown(s.left())
                break
            }
        }
        return sz
    }

    fun findOkSuffix(_r: Int, deb: Boolean = false, isOk: (T) -> Boolean): Int {
        if (_r < 0) return 0
        var test: T? = null
        var sz = 0
        val goDown = fun(_i: Int) {
            var i = _i
            while (i < 2 * N) {
//                if (deb) println(i)
                pushToChildren(i)
                val ntest: T = if (test != null) V[i] + test!! else V[i]
                if (isOk(ntest)) {
                    test = ntest
                    sz += SZ[i]
                    i = i.xor(1).right()
                } else i = i.right()
            }
        }
//        if (deb) print(segments(0, _r))
        for (s in segments(0, _r).reversed()) {
//            if (deb) println(s)
            var ntest: T = if (test != null) V[s] + test!! else V[s]
            if (isOk(ntest)) {
                test = ntest
                sz += SZ[s]
            } else {
                goDown(s.right())
                break
            }
        }
        return sz
    }
}
