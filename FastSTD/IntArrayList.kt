class IntArrayList : AbstractMutableList<Int> {
    companion object { val EMPTY_ARRAY = IntArray(0) }
    private val DEFAULT_CAPACITY = 10
    private val ENLARGE_SCALE = 2.0
    private var array: IntArray = EMPTY_ARRAY
    private var _size = 0
    override val size get() = _size
    constructor() {
        array = IntArray(DEFAULT_CAPACITY)
    }
    constructor(capacity: Int) {
        if (capacity > 0) array = IntArray(capacity)
    }
    override fun add(element: Int): Boolean {
        if (size == array.size) enlarge()
        array[_size++] = element
        return true
    }
    override fun add(index: Int, element: Int) {
        if (size == array.size) enlarge()
        for (i in _size downTo index+1)
            array[i] = array[i-1]
        array[index] = element
        _size++
    }
    override operator fun get(index: Int) = array[index]
    override fun removeAt(index: Int): Int {
        val ret = array[index]
        for (i in index+1 until _size)
            array[i] = array[i+1]
        _size--
        return ret
    }
    override operator fun set(index: Int, element: Int) = array[index].also { array[index] = element }
    override fun clear() { _size = 0 }
    private fun enlarge() {
        val newSize = max((_size + 1), (_size * ENLARGE_SCALE).toInt())
        val newArray = IntArray(newSize)
        System.arraycopy(array, 0, newArray, 0, _size)
        array = newArray
    }
}
