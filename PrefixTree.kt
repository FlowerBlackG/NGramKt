/*

    字典树结构

    自动分词
    《中文信息处理》大作业

    2051565 GTY

*/


internal data class PrefixTreeNode(
    /** 记录到当前位置为止表示的词登记了多少次。 */
    var counter: Long = 0L,

    /** 指向后续节点的索引。 */
    val link: HashMap<Char, PrefixTreeNode> = HashMap()
) {
    fun clear() {
        counter = 0
        link.clear()
    }
}


/**
 * 字典树结构。
 */
class PrefixTree {

    private val root = PrefixTreeNode()

    fun clear() {
        root.clear()
    }

    /**
     * 登记一个词。会同时登记由该词的前 m + 1, m + 2, ... , length 个字符组成的字词。可以通过传入参数控制。
     */
    fun put(str: String, ignoreDepth: Int = 0): PrefixTree {

        var curr = root
        var currDepth = 0
        str.forEach { ch ->
            if ( !curr.link.contains(ch) ) {

                curr.link[ch] = PrefixTreeNode()

            }

            curr = curr.link[ch]!!
            currDepth ++

            if (currDepth > ignoreDepth) {
                curr.counter++
            }
        }

        return this
    }

    /**
     * 寻找一个词在树上登记的次数。
     */
    fun search(str: String): Long {

        val depth = str.length

        var currDepth = 0
        var currNode = root

        for (ch in str) {

            if ( ! currNode.link.contains(ch) ) {
                currNode = root
                break
            }

            currNode = currNode.link[ch]!!
            currDepth ++

            if (currDepth >= depth) {
                break
            }
        }

        if (currDepth < depth) {
            currNode = root
        }

        return currNode.counter
    }

}


