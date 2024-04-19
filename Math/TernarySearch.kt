
fun ternary(lo: Double, hi: Double, eval: (Double) -> Double): Pair<Double,Double> {
    var lo = lo
    var hi = hi
    for (u in 0 until 100) {
        val m1 = lo + 1 * (hi - lo) / 3
        val m2 = lo + 2 * (hi - lo) / 3
        if (eval(m1) < eval(m2)) {
            hi = m2
        } else {
            lo = m1
        }
    }
    return Pair((lo+hi)/2, eval((lo + hi) / 2))
}
