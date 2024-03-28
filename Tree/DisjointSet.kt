class DisjointSet(val N: Int) {
    val P = IntArray(N) { -1 }
    fun find(x: Int) : Int {
        return if(P[x] < 0) x else { P[x] = find(P[x]); P[x] }
    }
    fun join(x: Int, y: Int): Boolean {
        var rx = find(x); var ry = find(y)
        if(rx == ry) return false;
        if(P[ry] < P[rx]) rx = ry.also { ry = rx }
        P[rx] += P[ry]
        P[ry] = rx
        return true
    }
}
