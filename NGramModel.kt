/*

    NGram 模型

    自动分词
    《中文信息处理》大作业

    2051565 GTY

*/

import kotlin.math.min


class NGramModel constructor(
    n: Int = 7
) {

    /** 滑动窗口大小。 */
    private val n = n

    /** 登记词出现次数的字典 */
    private val prefixTree = PrefixTree()

    fun clear() {
        prefixTree.clear()
    }

    fun train(sentences: List<String>) {

        clear() // 先删掉上一次训练残留的东西。

        for (sentence in sentences) {

            /** 滑动窗口的实际大小。当 n 大于整句长度时，滑动窗口应该减小到与句子长度匹配。 */
            val windowSize = min(n, sentence.length)

            for (idx in 0 .. sentence.length - min(2, windowSize)) {
                val windowStr = sentence.substring(idx, min(sentence.length, idx + windowSize)) // 截取窗口内的字串。
                prefixTree.put(windowStr, min(2, windowStr.length) - 1) // 登记到字典。
            }

        } // end of for (sentence in sentences)

    } // end of fun train(sentences: List<String>)


    fun dump(filename: String) {
        // todo: 懒得写了。
    }

    fun load(filename: String) {
        // todo: 懒得写了。
    }

    /**
     * 判断一个字符串是否仅由数字和英文字母组成。
     */
    private fun String.isPureDigitsAndEnglishLetters(): Boolean {
        for (ch in this) {
            if (!ch.isEnglishLetterOrDigit()) {
                return false // 只要有任何一个字符不是英文字母或数字，整句就不是“纯英文字母和数字组成”的。
            }
        }

        return true
    }

    /**
     * 处理一个句子片段。
     *
     * @param segment 待处理片段。如："同济大学"
     * @param container 存放处理结果的容器。处理结果（如 ["同济", "大学"] ）会被追加到该容器内。
     */
    private fun processSegment(segment: String, container: ArrayList<String>) {

        println("[info] processing: $segment")

        // 如果长度只有1，就不处理。
        // 如果是纯数字和纯英文，也不管。
        if (segment.length == 1 || segment.isPureDigitsAndEnglishLetters()) {
            container.add(segment)
            return
        }

        /** 滑动窗口开始位置。 */
        var windowStartPos = 0

        /** 滑动窗口大小。 */
        val windowSize = min(n, segment.length)

        while (windowStartPos < segment.length) {

            /** 最大分割产生点。 */
            var maxMidPos = 0

            /** 滑动窗口内的字串。 */
            val windowStr = segment.substring(windowStartPos, min(segment.length, windowStartPos + windowSize))

            /** 最大分割的第二段字符串的结尾位置（不含）。 */
            var maxEndPos = windowStr.length

            /** 最大出现次数值。 */
            var maxOcc = prefixTree.search(windowStr)

            /** 当前选中的分割方案里，左句出现次数。 */
            var maxSplitLeft = -1L

            /** 当前选中的分割方案里，右句出现次数。 */
            var maxSplitRight = -1L

            // 寻找最大的分割点。
            for (midPos in 1 until windowStr.length) {

                // 寻找最大的结尾。
                for (endPos in windowStr.length downTo  midPos + 1) {

                    /** 当前切分方式下，切出来的左句。 */
                    val leftStr = windowStr.substring(0, midPos)

                    /** 当前切分方式下，切出来的右边句。 */
                    val rightStr = windowStr.substring(midPos, endPos)

                    /** 当前切分方式下，左句登记的出现次数。 */
                    val leftOcc = prefixTree.search(leftStr)

                    /** 当前切分方式下，右句登记的出现次数。 */
                    val rightOcc = prefixTree.search(rightStr)

                    // 打印一些日志。
                    println("> left split  : $leftStr @ $leftOcc")
                    println("  right split : $rightStr @ $rightOcc")
                    println("  prev max occ: $maxOcc")
                    println("  prev lmax   : $maxSplitLeft")
                    println("  prev rmax   : $maxSplitRight")

                    /** 用于登记是否可以选用这种切分方案。“选用”不代表最终使用，当前方案可能会被后面更优的方案覆盖。 */
                    var canPick: Boolean


                    if (maxMidPos == 0) { // 首次切割。要求不要太高。

                        canPick = (leftOcc > maxOcc || rightOcc > maxOcc) && leftOcc + rightOcc >= 2 * maxOcc

                    } else { // 如果不是首次切分...

                        // 只要满足以下可切分条件的任何一个，就认为当前这个点是可以切分的。

                        // 切分后的子句出现次数和比之前登记的大一些，且左右词出现次数单独比较都没有降低太多。
                        canPick = leftOcc + rightOcc > maxOcc * 1.2 /* 放大系数 */
                                && (
                                    (leftOcc > maxSplitLeft * 1.6 && rightOcc > maxSplitRight * 0.8)
                                            || (rightOcc > maxSplitRight * 2 && leftOcc > maxSplitLeft * 0.8)
                                )

                        // 原来左词出现次数是0，新方案里结果不是0了，且右词出现次数没有降低太多。
                        canPick = canPick || (maxSplitLeft == 0L && (leftOcc >= 0 && rightOcc > maxSplitRight * 0.4))

                        // 左右词出现次数都有提升。
                        canPick = canPick || (leftOcc > maxSplitLeft && rightOcc >= maxSplitRight)


                        // 下面这部分感觉可以通过某种学习方法寻找，并且找到一个较好的数学函数来做决策。
                        // 但我不懂机器学习，所以就先这样吧。

                        canPick = canPick || (leftOcc > maxSplitLeft * 2 && rightOcc >= maxSplitRight * 0.5)
                        canPick = canPick || (leftOcc > maxSplitLeft * 5 && rightOcc >= maxSplitRight * 0.24)
                        canPick = canPick || (leftOcc > maxSplitLeft * 10 && rightOcc >= maxSplitRight * 0.05)

                        canPick = canPick || (rightOcc > maxSplitRight * 2 && leftOcc > maxSplitLeft * 0.7)
                        canPick = canPick || (rightOcc > maxSplitRight * 5 && leftOcc > maxSplitLeft * 0.24)
                        canPick = canPick || (rightOcc > maxSplitRight * 10 && leftOcc > maxSplitLeft * 0.05)

                    }

                    if (canPick) { // 更新选择的切分方法。
                        maxMidPos = midPos
                        maxEndPos = endPos
                        maxSplitLeft = leftOcc
                        maxSplitRight = rightOcc
                        maxOcc = leftOcc + rightOcc

                        println("  -- picked ! --")
                    }

                } // end of for (endPos in windowStr.length downTo  midPos + 1)

            } // end of for (midPos in 1 until windowStr.length)

            // 实施切分，并登记。

            if (maxMidPos == 0) {
                container.add(windowStr.substring(0, maxEndPos))
                windowStartPos += maxEndPos
                println("[info] extract(f): $windowStr")
            } else {
                val subSegment = windowStr.substring(0, maxMidPos)
                container.add(subSegment)
                windowStartPos += maxMidPos
                println("[info] extract: $subSegment")
            }

        } // end of while (windowStartPos < segment.length)

    } // end of private fun processSegment(segment: String, container: ArrayList<String>)

    /**
     * 切分一个句子。
     */
    fun process(sentence: String): List<String> {
        val res = ArrayList<String>()

        // 预处理。
        val segments = sentence.toProcessedListForNGram()

        // 对预处理后的每个子句依次分词。
        segments.forEach { segment ->
            processSegment(segment, res)
        }

        return res
    }

    /**
     * 获取一个词在字典内登记的次数。
     */
    fun getCount(str: String): Long {
        return prefixTree.search(str)
    }

}
