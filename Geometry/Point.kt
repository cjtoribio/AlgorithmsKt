class Point(val x: Double, val y: Double, val z: Double) {
    constructor(x: Number, y: Number, z: Number) :
            this(x.toDouble(), y.toDouble(), z.toDouble())
    operator fun plus(o: Point) = Point(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Point) = Point(x - o.x, y - o.y, z - o.z)
    operator fun unaryMinus() = Point(-x, -y, -z)
    fun mag() = sqrt(x*x + y*y + z*z)
    fun rotate90() = Point(-y, x, z)
    operator fun times(o: Number) = Point(x * o.toDouble(), y * o.toDouble(), z * o.toDouble())
    operator fun div(o: Number) = Point(x / o.toDouble(), y / o.toDouble(), z / o.toDouble())
    override fun toString(): String {
        return "($x, $y, $z)"
    }
}
