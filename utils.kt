/*

    工具

    自动分词
    《中文信息处理》大作业

    2051565 GTY

*/

import java.io.File


/**
 * 判断一个字符是否是数字或英文字母。
 */
fun Char.isEnglishLetterOrDigit(): Boolean {
    val code = this.code
    return isDigit() || (code >= 'a'.code && code <= 'z'.code) ||(code >= 'A'.code && code <= 'Z'.code)
}


/**
 * 从文件读入训练素材。
 *
 * @param filename 存储新闻数据的文件路径。
 * @param limit 读取的语料数量上限。
 *
 * @return 读取的文章数组。每个元素是一篇文章内容。未经过预处理。
 */
fun readSentences(filename: String, limit: Long = Long.MAX_VALUE): ArrayList<String> {

    val res = ArrayList<String>()
    val file = File(filename)

    /** 记录当前已经读入了多少条语料。 */
    var counter = 0L

    /** 文件里的所有内容。 */
    val lines = file.readLines()

    /** 上一次在界面里输出的读取进度。 */
    var prevReportProgress = 0

    for (idx in lines.indices) { // 对 lines 数组的下标做枚举...

        val line = lines[idx] // 当前循环到的那行数据。

        val currProgress = (idx + 1) * 100 / 10 / lines.size // 计算读取进度。

        if (currProgress > prevReportProgress) { // 输出读取进度。
            println("[info] loading sentences: ${currProgress * 10}% ...")
            prevReportProgress = currProgress
        }

        if (line.isBlank()) { // 跳过空行。
            continue
        }

        // 假设每行的包含多个信息，如文章标题、链接等。不同信息之间使用 \u0007 分割。最后一个元素是文章本体。
        // 该结构详解参考：https://blog.csdn.net/m0_62405272/article/details/124657772#t5
        res.add(line.split("\u0007").last())

        counter++ // 计数。
        if (counter >= limit) {
            break // 达到设定的上限。
        }

    } // end of for (idx in lines.indices)

    println("[info] sentences read.")
    return res
}


/**
 * 将字符串施加预处理。
 * 该函数适用于完成对一个句子做分词前，先行执行的预处理。
 * 运行过程会发生拷贝，不宜高密度使用。
 */
fun String.toProcessedListForNGram(): ArrayList<String> {
    val res = arrayListOf(this) // 构造一个仅含待分割词的列表。
    return res.removeStrangeTokens()
}


/**
 * 根据特殊字符，对字串进行分割。特殊字符会被删去。同时，也将数字和英文字符提取，与中文分开。
 *
 * @param this 包含文本的数组。每个元素是一条语料。该容器本身会在处理时被改变。
 *
 * @return 将传入的列表返回。列表会被修改。
 */
fun ArrayList<String>.removeStrangeTokens(): ArrayList<String> {
    println("[info] preparing to preprocess sentences...")

    val res = this

    // 按照标点符号分词。
    // 这个正则表达式是 new bing 帮我设计的。感谢她！
    val splitRegex = "[\\s${Regex.escape("\u3000.,!@#\$%^&*()-_+=/?:;`~'\"[]{}|\\，。《》？！￥…（）【】·‘’“”；：—、\t\n")}]+".toRegex()

    for (counter in 0 until res.size) {

        // 拿出第一个元素。
        val sentence = res.removeFirst()

        // 根据正则表达式切分，并删去切分后为空的句子。
        val subSentences = sentence.split(splitRegex).filter { it.isNotBlank() }

        // 把分割成的结果放到最后面。
        res += subSentences
    }


    println("[info] preprocess: strange symbol removed.")

    /** 上次汇报进度时，的进度。 */
    var prevReportProgress = 0

    // 按照英文和数字分词
    // 目标：将中文句子里的非汉字内容提取。
    // 如：
    //   输入：同济大学2023年第一学期
    //   切分结果：[同济大学, 2023, 年第一学期]
    val originalSize = res.size

    for (counter in 0 until originalSize) {

        val currProgress = (counter + 1) * 100 / 10 / originalSize // 当前进度计数。

        if (currProgress > prevReportProgress) { // 输出处理进度。
            prevReportProgress = currProgress
            println("[info] preprocess: ${currProgress * 10}%")
        }

        // 拿出第一个句子。
        val sentence = res.removeFirst()

        /*

            切分思路：
              记录一个状态 M，表示“开始位置的字符是字母或数字”
              如：开始位置为“厨”时，M=0
                  开始位置为“a”时，M=1
                  开始位置为“6”时，M=1

              记“当前位置的字符时字母或数字”为 N

              最初，N = M。之后，对于每个位置，如果发现 N 与 M 一直相同，表示这段文本的“状态性质”是一致的。
              如：“同济大学”，M = 0，N 一直也是 0
              但对于“2023年”，M = 1，当前位置指向“年”时，N 变成 0，与 M 不同了。

              因此，只要当 M 和 N 不同，就把当前位置以前的部分全部提取，单独当作一个词。如此即可完成切分。

        */

        /** 前述“开始位置”对应当前待处理字串内的位置。 */
        var start = 0

        /** 开始位置的字符是字母或数字？ */
        var startIsLOD = sentence.first().isEnglishLetterOrDigit() // marks is `start` pointed char a letter or digit ?

        for (idx in sentence.indices) {

            /** 当前位置字符。 */
            val c = sentence[idx]

            /** 当前位置的字符是字母或数字？ */
            val thisIsLOD = c.isEnglishLetterOrDigit()

            if (thisIsLOD xor startIsLOD) { // 亦或结果为 1，表示 M 与 N 不同。

                // 提取句子，加入分割结果列表。
                res.add(sentence.substring(start, idx))

                // 把“当前位置”作为新的“开始位置”
                start = idx
                startIsLOD = thisIsLOD
            }

        }

        // 上面的流程会漏掉最后一个句子的最后一个切分子句。手动加进去。
        res.add(sentence.substring(start, sentence.length))

    }

    return res
}
