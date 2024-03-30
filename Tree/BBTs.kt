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
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random
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
    abstract class Node<T, N : Node<T, N>>(val x : T) {
        var left: N? = null
        var right: N? = null
        var parent: N? = null
        var sz: Int = 1
        fun isLeft(): Boolean {
            return parent?.left == this
        }
        open fun update(): N {
            sz = 1 + (left?.sz ?: 0) + (right?.sz ?: 0)
            return this as N
        }
    }

    protected fun <N : Node<T, N>> moveUp(n: N) {
        val p = n.parent!!
        val gp= p.parent
        if (p.left == n) {
            p.left = n.right; n.right = p; p.left?.parent = p
            n.parent = gp; p.parent = n
        } else {
            p.right = n.left; n.left = p; p.right?.parent = p
            n.parent = gp; p.parent = n
        }
        if (gp != null) {
            if (gp.left == p) gp.left = n
            else gp.right = n
        }
        p.update()
        n.update()
    }

    fun <N : Node<T, N>> iterator(root : N?): MutableIterator<T> {
        val stack = Stack<Node<T, N>>()
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
}

class TreapSet<T : Comparable<T>> : AbstractMutableSet<T>() {
    inner class Node(val x: T) {
        var left: Node? = null
        var right: Node? = null
        var sz: Int = 1
        val y = Random.nextInt()
        fun update(): Node {
            sz = 1 + (left?.sz ?: 0) + (right?.sz ?: 0)
            return this
        }
    }

    var root: Node? = null

    override val size: Int get() = root?.sz ?: 0
    private fun split(t: Node?, goLeft: (Node) -> Boolean) : Pair<Node?, Node?> {
        val ret = if (t == null) {
            Pair(null, null)
        } else if (goLeft(t)) {
            val (l, r) = split(t.left, goLeft); t.left = r; t.update(); Pair(l, t)
        } else {
            val (l, r) = split(t.right, goLeft); t.right = l; t.update(); Pair(t, r)
        }
        return ret
    }
    private fun insert(t: Node?, it: Node): Node {
        val ret = if (t == null) it
        else if(it.y > t.y){
            val (l, r) = split(t) { n -> it.x < n.x }
            it.left = l; it.right = r; it
        }else{
            if (it.x < t.x) { t.left = insert(t.left, it); t }
            else { t.right = insert(t.right, it); t }
        }
        ret.update()
        return ret
    }

    override fun add(element: T): Boolean {
        root = insert(root, Node(element)).update()
        return true
    }

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
}

class AVLTree<T : Comparable<T>> : BinaryBalanceTree<T>() {
    inner class Node(x: T) : BinaryBalanceTree.Node<T, Node>(x) {
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

    private fun insert(nn: Node) : Node {
        var n = root
        while (n != null) {
            if (nn.x < n.x) {
                if (n.left == null) { n.left = nn; nn.parent = n; n.update(); break }
                else n = n.left
            } else {
                if (n.right == null) { n.right = nn; nn.parent = n; n.update(); break }
                else n = n.right
            }
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
}

class SplayTree<T : Comparable<T>> : BinaryBalanceTree<T>() {
    inner class Node(x: T) : BinaryBalanceTree.Node<T, Node>(x)

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
            else if (n.isLeft() == n.parent!!.isLeft()) {
                moveUp(n.parent!!)
                moveUp(n)
            } else {
                moveUp(n)
                moveUp(n)
            }
        }
    }

    override fun add(v: T) : Boolean {
        insert(Node(v))
        return true
    }

    private fun insert(nn: Node) {
        var n = root
        while (n != null) {
            if (nn.x < n.x) {
                if (n.left == null) { n.left = nn; nn.parent = n; break }
                else n = n.left
            } else {
                if (n.right == null) { n.right = nn; nn.parent = n; break }
                else n = n.right
            }
        }
        splay(nn)
        root = nn
    }

}

fun main() {

//    Reader.takeFile("./in.txt")
    val duration = measureTime {
        val a = AVLTree<Int>()

        for (i in 0 until 400_000) {
            a.add(randInt())
        }
//        println(a.take(100).joinToString("\n"))
//        println(a.ops)
    }
    println(duration)


}
