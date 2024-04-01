import java.io.BufferedInputStream
import java.io.File
import java.io.PrintWriter
import java.time.Duration
import java.time.Instant
import java.util.AbstractSet
import java.util.PriorityQueue
import java.util.Stack
import java.util.TreeMap
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.random.Random
import kotlin.reflect.full.primaryConstructor
import kotlin.time.measureTime
import kotlin.time.toKotlinDuration

// 1. Modded
const val MOD_P = 998_244_353
fun Int.adjust():Int{ if(this >= MOD_P){ return this  - MOD_P }else if (this < 0){ return this + MOD_P };return this }
fun Int.snap():Int{ if(this >= MOD_P){return this - MOD_P} else return this}
infix fun Int.mm(b:Int):Int{ return ((this.toLong() * b) % MOD_P).toInt() }
infix fun Int.mp(b:Int):Int{ val ans = this + b;return if(ans >= MOD_P) ans - MOD_P else ans }
infix fun Int.ms(b:Int):Int{ val ans = this - b;return if(ans < 0) ans + MOD_P else ans }
fun Long.modded():Int = (this % MOD_P).toInt().adjust()
fun solveLinearCongruence(a: Int, b: Int, m: Int): Int { // ~2M per second
    if (b == 0 || b == m) return 0
    if (a == 0) return -1
    val y = solveLinearCongruence(m % a, -b % a + a, a);
    return if (y < 0) y else ((1L*m*y+b) / a).toInt()
}
fun Int.inverse():Int = solveLinearCongruence(this, 1, MOD_P)
fun Int.additiveInverse():Int = if(this == 0) 0 else MOD_P - this
infix fun Int.modDivide(b:Int):Int{ return this mm (b.inverse()) }
infix fun Int.intPow(e:Int):Int{
    var x = this; var e = e ; var ret = 1
    while(e > 0){
        if(e and 1 == 1) ret = ret mm x
        x = x mm x
        e = e shr 1
    }
    return ret
}

@JvmField
val INPUT = System.`in`
@JvmField
val OUTPUT = System.out

@JvmField
var _reader = INPUT.bufferedReader()


// From https://codeforces.com/contest/1918/submission/244097805
const val interactive = false
object Reader{
    private const val BS = 1 shl 16
    private const val NC = 0.toChar()
    private val buf = ByteArray(BS)
    private var bId = 0
    private var size = 0
    private var c = NC
    private var IN: BufferedInputStream = BufferedInputStream(System.`in`, BS)
    val OUT: PrintWriter = PrintWriter(System.out)
    private val char: Char
        get() {
            if(interactive) return System.`in`.read().toChar()
            while (bId == size) {
                size = IN.read(buf)
                if (size == -1) return NC
                bId = 0
            }
            return buf[bId++].toChar()
        }
    fun nextLong(): Long {
        var neg = false
        if (c == NC) c = char
        while (c < '0' || c > '9') {
            if (c == '-') neg = true
            c = char
        }
        var res = 0L
        while (c in '0'..'9') {
            res = (res shl 3) + (res shl 1) + (c - '0')
            c = char
        }
        return if (neg) -res else res
    }
    fun nextString():String{
        val ret = StringBuilder()
        while (true){
            c = char
            if(!isWhitespace(c)){ break}
        }
        ret.append(c)
        while (true){
            c = char
            if(isWhitespace(c)){ break}
            ret.append(c)
        }
        return ret.toString()
    }
    fun isWhitespace(c:Char):Boolean{
        return c == ' ' || c == '\n' || c == '\r' || c == '\t'
    }
    fun flush(){
        OUT.flush()
    }
    fun takeFile(name:String){
        IN = BufferedInputStream(File(name).inputStream(), BS)
    }
}
fun done(){ Reader.OUT.close() }
fun readInt(): Int { val ans = Reader.nextLong() ; if(ans > Int.MAX_VALUE){IntArray(1000000000);error("Input Overflow")};return ans.toInt() }
fun readDouble() = Reader.nextString().toDouble()
fun readLong() = Reader.nextLong()
fun readString() = Reader.nextString()
fun readStrings(n: Int) = Array(n) { Reader.nextString() }
fun readInts(n: Int) = IntArray(n) { readInt() }
fun readDoubles(n: Int) = DoubleArray(n) { readDouble() }
fun readLongs(n: Int) = LongArray(n) { readLong() }

object MyRand {
    val _myRand = Random(979687456)
}
fun randInt(lo: Int = 1, hi: Int = 1000000000) = MyRand._myRand.nextInt(lo, hi+1)
fun randInts(n: Int, lo: Int = 1, hi: Int = 1000000000) = IntArray(n) { MyRand._myRand.nextInt(lo, hi+1) }
fun randLongs(n: Int) = IntArray(n) { MyRand._myRand.nextInt() }

@JvmField
val _writer = PrintWriter(OUTPUT, false)

inline fun output(block: PrintWriter.() -> Unit) {
    _writer.apply(block).flush()
}


object solver {

    inline fun single(case: () -> Unit) {
        val st = Instant.now()
        case()
        System.out.println(Duration.between(st, Instant.now()))
    }
    inline fun cases(limit: Int = 1_000_000, case: () -> Unit) {
        val TC = readInt()
        var limit = limit
        for (tc in 1..TC) {
            if (limit-- < 0) break
//            val st = Instant.now()
            case()
//            System.err.println(Duration.between(st, Instant.now()))
        }
    }
}

inline fun rep(N: Int, block: () -> Unit) {
    for (i in 0 until N)
        block()
}

abstract class BinaryBalanceTree<T : Comparable<T>> : AbstractMutableSet<T>() {
    var drillingDown = 0
    var rotations = 0
    open class Node<T, N : Node<T, N>>(val x : T, val child: Array<N?>) {
        val left get() = child[0]
        val right get() = child[1]
        var parent: N? = null
        var sz: Int = 1
        fun isLeft(): Boolean {
            return parent?.child?.get(0) == this
        }
        fun side(): Int {
            return if (parent?.child?.get(0) == this) 0 else 1
        }
        fun get(side: Int) = child[side]
        open fun update(): N {
            sz = 1 + ((child[0])?.sz ?: 0) + ((child[1])?.sz ?: 0)
            return this as N
        }
        fun attach(side: Int, ch: N?) {
            child[side] = ch
            ch?.parent = this as N
        }
    }

    protected fun <N : Node<T, N>> moveUp(n: N) {
        rotations++
        val p = n.parent!!
        val side = n.side()
        p.parent?.child?.set(p.side(), n)
        n.parent = p.parent
        p.child[side] = n.child[1 - side]
        n.child[1-side]?.parent = p
        n.child[1-side] = p
        p.parent = n
        p.update()
        n.update()
    }

    fun <N : Node<T, N>>contains(root: N?, element: T): Boolean {
        var n = root
        while (n != null) {
            drillingDown++
            n = when (element.compareTo(n.x).sign) {
                -1 -> n.left
                0 -> return true
                1 -> n.right
                else -> return false
            }
        }
        return false
    }

    fun <N : Node<T, N>> iterator(root : N?): MutableIterator<T> {
        val stack = Stack<Node<T, N>>()
        fun goAllLeft() {
            while (stack.isNotEmpty() && stack.peek().child[0] != null)
                stack.add(stack.peek().child[0])
        }
        if (root != null) stack.add(root)
        goAllLeft()
        return object : MutableIterator<T> {
            override fun hasNext() = stack.isNotEmpty()

            override fun next(): T {
                val t = stack.pop()
                if (t.child[1] != null) {
                    stack.add(t.child[1] as N)
                    goAllLeft()
                }
                return t.x
            }

            override fun remove() {
                TODO("Not yet implemented")
            }

        }
    }
}


typealias TreapSet<T> = TreapSegmentTree<T, Any>
class TreapSegmentTree<T: Comparable<T>, U>(val comparator: Comparator<T> = Comparator.naturalOrder()) : AbstractMutableSet<T>() {
    inner class Node(var x: T, var u: U? = null) {
        var left: Node? = null
        var right: Node? = null
        var flipPending: Boolean = false
        var sz: Int = 1
        val y = Random.nextInt()
        var ax = x
        fun flip() {
            flipPending = true
            left = right.also { right = left }
        }
        fun apply(u: U) {
            this.u += u!!
            ax = update!!(sz, x, u)
            x = update!!(1, x, u)
        }
        fun push() {
            if (flipPending) {
                flipPending = false
                left?.flip()
                right?.flip()
            }
            if (update != null && u != null) {
                left?.apply(u!!)
                right?.apply(u!!)
                u = null
            }
        }
        fun update(): Node {
            sz = 1 + (left?.sz ?: 0) + (right?.sz ?: 0)
            if (combiner != null) ax = left?.ax + x + right?.ax
            return this
        }
    }

    private operator fun T?.plus(other: T?): T {
        if (this == null) return other!!
        if (other == null) return this
        return combiner!!(this, other)
    }

    private operator fun U?.plus(other: U?): U {
        if (this == null) return other!!
        if (other == null) return this
        return updateCombiner!!(this, other)
    }

    var root: Node? = null
    var update: ((Int, T, U) -> T)? = null
    var combiner: ((T, T) -> T)? = null
    var updateCombiner: ((U, U) -> U)? = null

    override val size: Int get() = root?.sz ?: 0
    private fun split(t: Node?, goLeft: (Node) -> Boolean) : Pair<Node?, Node?> {
        t?.push()
        val ret = if (t == null) {
            Pair(null, null)
        } else if (goLeft(t)) {
            val (l, r) = split(t.left, goLeft); t.left = r; t.update(); Pair(l, t)
        } else {
            val (l, r) = split(t.right, goLeft); t.right = l; t.update(); Pair(t, r)
        }
        return ret
    }

    private fun join(l: Node?, r: Node?) : Node? {
        if (l == null) return r
        if (r == null) return l
        return if (l.y >= r.y) {
            l.push()
            l.right = join(l.right, r)
            l.update()
        }
        else {
            r.push()
            r.left = join(l, r.left)
            r.update()
        }
    }

    private fun insert(t: Node?, it: Node, goLeft: (Node) -> Boolean): Node {
        return if (t == null) it
        else if(it.y > t.y){
            val (l, r) = split(t, goLeft)
            it.left = l; it.right = r; it
        }else{
            t.push()
            if (it.x < t.x) { t.left = insert(t.left, it, goLeft); t }
            else { t.right = insert(t.right, it, goLeft); t }
        }.update()
    }

    private fun split(node: Node?, leftCount: Int): Pair<Node?, Node?> {
        var idxI = leftCount
        val (l, r) = split(node) { n -> if (idxI <= (n.left?.sz ?: 0)) true else { idxI -= (n.left?.sz ?: 0)+1; false } }
        return Pair(l, r)
    }

    fun update(i: Int, j: Int, u: U) {
        val (l1, r) = split(root, j+1)
        val (l, m) = split(l1, i)
        m?.apply(u)
        root = join(l, join(m, r))
    }

    fun query(i: Int, j: Int): T {
        val (l1, r) = split(root, j+1)
        val (l, m) = split(l1, i)
        val ret = m!!.ax
        if (ret == 13) {
            val t = 1
        }
        root = join(l, join(m, r))
        return ret
    }

    fun flip(i: Int, j: Int) {
        val (l1, r) = split(root, j+1)
        val (l, m) = split(l1, i)
        m?.flip()
        root = join(l, join(m,r))
    }

    fun insert(i: Int, element: T) : Boolean {
        val (l, r) = split(root, i)
        root = join(join(l, Node(element)), r)
        return true
    }

    override fun add(element: T): Boolean {
        root = insert(root, Node(element)) { n -> comparator.compare(element, n.x) < 0 }
        return true
    }

    override fun contains(element: T): Boolean {
        var n = root
        while (n != null) {
            n.push()
            n = when (comparator.compare(element, n.x).sign) {
                -1 -> n.left
                0 -> return true
                1 -> n.right
                else -> return false
            }
        }
        return false
    }

    override fun iterator(): MutableIterator<T> {
        val stack = Stack<Node>()
        fun goAllLeft() {
            while (stack.isNotEmpty() && stack.peek().left != null) {
                stack.peek().push()
                stack.add(stack.peek().left)
            }
        }
        if (root != null) stack.add(root)
        goAllLeft()
        return object : MutableIterator<T> {
            override fun hasNext() = stack.isNotEmpty()

            override fun next(): T {
                val t = stack.pop()
                if (t.right != null) {
                    stack.add(t.right)
                    goAllLeft()
                }
                return t.x
            }

            override fun remove() {
                TODO("Not yet implemented")
            }

        }
    }
}

class AVLTree<T : Comparable<T>> : BinaryBalanceTree<T>() {
    inner class Node(x: T) : BinaryBalanceTree.Node<T, Node>(x, Array(2) { null }) {
        var depth = 0
        fun balanceFactor() : Int {
            return (left?.depth?:-1) - (right?.depth?:-1)
        }
        override fun update(): Node {
            depth = max(left?.depth?:0, right?.depth?:0) + 1
            return super.update()
        }
    }
    var root: Node? = null
    override fun iterator(): MutableIterator<T> {
        return super.iterator(root)
    }

    override val size get() = root?.sz ?: 0
    override fun add(element: T): Boolean {
        root = insert(Node(element))
        return true
    }

    override fun contains(element: T): Boolean {
        return contains(root, element)
    }

    private fun insert(nn: Node) : Node {
        var n = root
        while (n != null) {
            drillingDown++
            val side = if (nn.x < n.x) 0 else 1
            if (n.child[side] == null) { n.attach(side, nn); n.update(); break }
            else n = n.get(side)
        }
        if (n == null) n = nn
        while (true) {
            n = balance(n!!)
            if (n.parent == null) break
            n = n.parent
        }
        return n!!
    }

    fun balance(n: Node) : Node {
        val bf = n.balanceFactor()
        return if (bf == -2) {
            val r = n.right!!
            if (r.balanceFactor() == 1) {
                val l = r.left!!
                moveUp(l); moveUp(l); l
            } else {
                moveUp(r); r
            }
        } else if (bf == 2) {
            val l = n.left!!
            if (l.balanceFactor() == -1) {
                val r = l.right!!
                moveUp(r); moveUp(r); r
            } else {
                moveUp(l); l
            }
        } else n.update()
    }

    fun treePrint(n: Node? = root, indent: Int = 0) {
        if (n == null) return
        treePrint(n.left, indent+1)
        println("   ".repeat(indent) + "${n.x}")
        treePrint(n.right, indent+1)
    }
}

class RBTree<T :  Comparable<T>> : BinaryBalanceTree<T>() {
    inner class Node(x: T) : BinaryBalanceTree.Node<T, Node>(x, Array(2) { null }) {
        var red = true
    }

    private fun fixAfterInsert(n: Node) {
        var n = n
        n.red = true
        while (n.parent?.red == true) {
            val p = n.parent!!
            val y = p.parent!!.child[1 - p.side()]
            if (y?.red == true) {
                p.red = false
                y.red = false
                p.parent!!.red = true
                n = p.parent!!
            } else {
                if (n.side() != p.side()) {
                    moveUp(n)
                    n.red = false
                    n.parent?.red = true
                    moveUp(n)
                    n = p
                } else {
                    p.red = false
                    p.parent?.red = true
                    moveUp(p)
                }
            }
        }
        while (n.parent != null) {
            n = n.parent!!
            n.update()
        }
        root = n
        root?.red = false
    }

    var root: Node? = null
    override fun iterator(): MutableIterator<T> {
        return super.iterator(root)
    }

    override fun contains(element: T) : Boolean {
        return contains(root, element)
    }
    override val size get() = root?.sz ?: 0
    override fun add(element: T): Boolean {
        val nn = Node(element)
        var n = root
        while (n != null) {
            drillingDown++
            val side = if (nn.x < n.x) 0 else 1
            if (n.child[side] == null) { n.attach(side, nn); n.update(); break }
            else n = n.get(side)
        }
        fixAfterInsert(nn)
        return true
    }


}

class SplayTree<T : Comparable<T>> : BinaryBalanceTree<T>() {
    inner class Node(x: T) : BinaryBalanceTree.Node<T, Node>(x, Array(2) { null })

    var root: Node? = null
    override val size get() = root?.sz ?: 0
    override fun iterator(): MutableIterator<T> {
        val stack = Stack<Node>()
        fun goAllLeft() {
            while (stack.isNotEmpty() && stack.peek().left != null)
                stack.add(stack.peek().left)
        }
        if (root != null) stack.add(root)
        goAllLeft()
        return object : MutableIterator<T> {
            override fun hasNext() = stack.isNotEmpty()

            override fun next(): T {
                val t = stack.pop()
                if (t.right != null) {
                    stack.add(t.right)
                    goAllLeft()
                }
                return t.x
            }

            override fun remove() {
                TODO("Not yet implemented")
            }

        }
    }

    private fun splay(n: Node) {
        while (n.parent != null) {
            if (n.parent!!.parent == null) moveUp(n)
            else if (n.side() == n.parent?.side()) {
                moveUp(n.parent!!)
                moveUp(n)
            } else {
                moveUp(n)
                moveUp(n)
            }
        }
        root = n
    }

    override fun add(v: T) : Boolean {
        insert(Node(v))
        return true
    }

    override fun contains(element: T) : Boolean {
        var n = root
        while (n != null && n.x != element) {
            drillingDown++
            if (element < n.x) {
                if (n.left == null) break
                else n = n.left
            } else {
                if (n.right == null) break
                else n = n.right
            }
        }
        if (n != null) splay(n)
        return n?.x == element
    }

    private fun insert(nn: Node) {
        var n = root
        while (n != null) {
            drillingDown++
            if (nn.x < n.x) {
                if (n.left == null) { n.attach(0, nn); break }
                else n = n.left
            } else {
                if (n.right == null) { n.attach(1, nn); break }
                else n = n.right
            }
        }
        splay(nn)
    }

}

fun benchmark() {

    val setTypes: Map<String, () -> AbstractSet<Int>> = mapOf(
        Pair("RBTree") { RBTree() },
        Pair("AvlTree") { AVLTree() },
        Pair("TreapSet") { TreapSet() },
        Pair("TreeSet") { TreeSet() },
        Pair("SplayTree") { SplayTree() }
    )

    val times = 400_000
    for (i in 1..2  ) {
        for ((name, builder) in setTypes) {
            val a = builder()
            val insertDuration = measureTime {
                for (i in 0 until times) {
                    a.add(randInt())
//            a.treePrint()
//            println("-".repeat(20))
                }
//                println("rotations=${a.rotations}")
//                println("drillingDown=${a.drillingDown}")
            }
            val accessDuration = measureTime {
//                a.rotations = 0
//                a.drillingDown = 0
                for (i in 0 until times) {
                    a.contains(randInt())
//            a.treePrint()
//            println("-".repeat(20))
                }
//                println("rotations=${a.rotations}")
//                println("drillingDown=${a.drillingDown}")

//        println(a.take(100).joinToString("\n"))
//        println(a.ops)
            }
            println("${name} ${insertDuration} ${accessDuration} ${a.size}")
        }
        println("-".repeat(20))
    }
}


fun main() {

//    Reader.takeFile("./in.txt")

//    benchmark()
    val tr = TreapSegmentTree<Int, Int>()

    tr.combiner = { a,b -> a + b }
    tr.update = {sz, v, u -> v + sz * u}
    tr.updateCombiner = { a, b -> a + b }


    tr.insert(0, 10)
    tr.insert(0, 9)
    tr.insert(0, 8)
    tr.insert(0, 7)
    tr.insert(2, 6)
    tr.insert(4, 5)

    println(tr.query(0, 2))
    println(tr)

    tr.flip(1,5)
//    tr.update(0, 2, 1)

    println(tr.query(0, 2))
    println(tr)


}
