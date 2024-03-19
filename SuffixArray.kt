class VirtualIntArray(val arr: IntArray, val offset: Int) {
    val real = arr
    operator fun get(i: Int): Int {
        return real[offset + i]
    }
    operator fun set(i: Int, v: Int) {
        real[offset + i] = v
    }
}
fun IntArray.virtual(offset: Int): VirtualIntArray {
    return VirtualIntArray(this, offset)
}

class SuffixArray(val S : String) {
    val N = S.length
    val T = IntArray(N + 3)
    var SA = IntArray(N + 3)
    val RA = IntArray(N)
    val LCP = IntArray(N)
    init {
        for (i in S.indices) T[i] = S[i].code
        T[N] = 0; T[N+1] = 0; T[N+2] = 0
        suffixArray(T, SA, N, 256)
        for (i in 0 until N) RA[SA[i]] = i
        SA = IntArray(N) { SA[it] }
    }
    fun leq(a1: Int, a2: Int, b1: Int, b2: Int) : Boolean { // lexicographic order
        return (a1 < b1 || a1 == b1 && a2 <= b2)
    } // for pairs
    fun leq(a1: Int, a2: Int, a3: Int, b1: Int, b2: Int, b3: Int) : Boolean {
        return (a1 < b1 || a1 == b1 && leq(a2, a3, b2, b3))
    } // and triples
    // stably sort a[0..n-1] to b[0..n-1] with keys in 0..K from r
    fun radixPass(a: IntArray, b: IntArray, r: VirtualIntArray, n: Int, K: Int) { // count occurrences
        val c = IntArray(K + 1) // counter array
        for (i in 0 until n)
            c[r[a[i]]]++; // count occurrences
        var sum = 0
        for (i in 0..K) { // exclusive prefix sums
            val t = c[i];
            c[i] = sum;
            sum += t;
        }
        for (i in 0 until n)
            b[c[r[a[i]]]++] = a[i]; // sort
    }
    fun suffixArray(T: IntArray, SA: IntArray, n: Int, K: Int) {
        val n0 = (n + 2) / 3; val n1 = (n + 1) / 3; val n2 = n / 3; val n02 = n0 + n2
        val R = IntArray(n02 + 3) // R[n02] = R[n02 + 1] = R[n02 + 2] = 0
        val SA12 = IntArray(n02 + 3) // SA12[n02] = SA12[n02 + 1] = SA12[n02 + 2] = 0;
        val R0 = IntArray(n0)
        val SA0 = IntArray(n0)
        //******* Step 0: Construct sample ********
        // generate positions of mod 1 and mod 2 suffixes
        // the "+(n0-n1)" adds a dummy mod 1 suffix if n%3 == 1
        if (true) {
            var j = 0
            for (i in 0 until n + (n0 - n1))
                if (i % 3 != 0)
                    R[j++] = i;
        }
        //******* Step 1: Sort sample suffixes ********
        // lsb radix sort the mod 1 and mod 2 triples
        radixPass(R, SA12, T.virtual(2), n02, K)
        radixPass(SA12, R, T.virtual(1), n02, K)
        radixPass(R, SA12, T.virtual(0), n02, K)
        // find lexicographic names of triples and
        // write them to correct places in R
        var name = 0; var c0 = -1; var c1 = -1; var c2 = -1
        for (i in 0 until n02) {
            if (T[SA12[i]] != c0 || T[SA12[i] + 1] != c1 || T[SA12[i] + 2] != c2) {
                name++;
                c0 = T[SA12[i]];
                c1 = T[SA12[i] + 1];
                c2 = T[SA12[i] + 2];
            }
            if (SA12[i] % 3 == 1) {
                R[SA12[i] / 3] = name;
            } // write to R1
            else {
                R[SA12[i] / 3 + n0] = name;
            } // write to R2
        }
        // recurse if names are not yet unique
        if (name < n02) {
            suffixArray(R, SA12, n02, name);
            // store unique names in R using the suffix array
            for (i in 0 until n02)
                R[SA12[i]] = i + 1;
        } else {
            // generate the suffix array of R directly
            for (i in 0 until n02)
                SA12[R[i] - 1] = i;
        }
        //******* Step 2: Sort nonsample suffixes ********
        // stably sort the mod 0 suffixes from SA12 by their first character
        if (true) {
            var j = 0
            for (i in 0 until n02)
                if (SA12[i] < n0)
                    R0[j++] = 3 * SA12[i];
            radixPass(R0, SA0, T.virtual(0), n0, K);
        }
        //******* Step 3: Merge ********
        // merge sorted SA0 suffixes and sorted SA12 suffixes
        if (true) {
            var t = n0-n1; var p = 0
            fun getI(): Int {
                return if(SA12[t] < n0) SA12 [t] * 3 + 1 else (SA12[t]-n0) * 3+2
            }
            var k = 0
            while (k < n) {
                val i = getI(); // pos of current offset 12 suffix
                val j = SA0[p]; // pos of current offset 0 suffix
                if (if (SA12[t] < n0) // different compares for mod 1 and mod 2 suffixes
                        leq(T[i], R[SA12[t] + n0], T[j], R[j / 3]) else leq(
                        T[i], T[i + 1],
                        R[SA12[t] - n0 + 1], T[j], T[j + 1], R[j / 3 + n0]
                    )
                ) { // suffix from SA12 is smaller
                    SA[k] = i;
                    t++;
                    if (t == n02) {// done --- only SA0 suffixes left
                        k++
                        while (p < n0) {
                            SA[k] = SA0[p]
                            p++; k++
                        }
                    }
                } else { // suffix from SA0 is smaller
                    SA[k] = j;
                    p++;
                    if (p == n0) {// done --- only SA12 suffixes left
                        k++
                        while (t < n02) {
                            SA[k] = getI();
                            t++; k++
                        }
                    }
                }
                k++
            }
        }
    }
    fun buildLCP() {
        val PLCP = IntArray(N)
        val PHI = IntArray(N)
        PHI[SA[0]] = -1
        for (i in 1 until N)
            PHI[SA[i]] = SA[i - 1]
        var L = 0
        for (i in 0 until N) {
            if (PHI[i] == -1) {
                PLCP[i] = 0
                continue
            }
            while (PHI[i] + L < N && i + L < N && T[i + L] == T[PHI[i] + L])
                L++
            PLCP[i] = L
            L = max(L - 1, 0);
        }
        for (i in 1 until N)
            LCP[i] = PLCP[SA[i]]
    }
    var RLCP = Array(0) { IntArray(0) }
    var LOG2 = IntArray(0)
    fun buildRangeQueries(){
        var L = 0
        while((1 shl L) <= N) L++;
        val RLCP = Array(L) { IntArray(N) }
        for (i in 0 until N) {
            RLCP[0][i] = LCP[i]
        }
        for (i in 1 until L) {
            for (j in 0 until N)
                RLCP[i][j] = min( RLCP[i-1][j] , if (j<(1 shl i)) 0 else RLCP[i-1][j-(1 shl (i-1))] )
        }
        LOG2 = IntArray(N) { -1 }
        for (i in 0 until L) {
            if ((1 shl i) >= N) break
            LOG2[1 shl i] = i
        }
        for (i in 1 until N) {
            if(LOG2[i] == -1){
                LOG2[i] = LOG2[i-1];
            }
        }
    }
    fun lcp(i: Int, j: Int): Int {
        if (i == j) return N - SA[i]
        val l = LOG2[j - i]
        return min(RLCP[l][j], RLCP[l][i + (1 shl l)])
    }
}
