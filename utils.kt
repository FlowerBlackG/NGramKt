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

    var prevReportProgress = 0
    for (idx in lines.indices) {

        val line = lines[idx]

        val currProgress = (idx + 1) * 100 / 10 / lines.size // 读取进度。
        if (currProgress > prevReportProgress) { // 输出读取进度。
            println("[info] loading sentences: ${currProgress * 10}% ...")
            prevReportProgress = currProgress
        }

        if (line.isBlank()) { // 跳过空行。
            continue
        }

        // 假设每行的包含多个信息，如文章标题、链接等。不同信息之间使用 \u0007 分割。最后一个元素是文章本体。
        res.add(line.split("\u0007").last())

        counter++
        if (counter >= limit) {
            break // 达到设定的上限。
        }
    }

    println("[info] sentences read.")
    return res
}


/**
 * 将字符串施加预处理。
 * 运行过程会发生拷贝，不宜高密度使用。
 */
fun String.toProcessedListForNGram(): ArrayList<String> {
    val res = arrayListOf(this)
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

        val sentence = res.removeFirst()
        val subSentences = sentence.split(splitRegex).filter { it.isNotBlank() }

        res += subSentences
    }


    println("[info] preprocess: strange symbol removed.")

    var prevReportProgress = 0
    // 按照英文和数字分词
    val originalSize = res.size
    for (counter in 0 until originalSize) {

        val currProgress = (counter + 1) * 100 / 10 / originalSize
        if (currProgress > prevReportProgress) {
            prevReportProgress = currProgress
            println("[info] preprocess: ${currProgress * 10}%")
        }

        val sentence = res.removeFirst()

        var start = 0
        var startIsLOD = sentence.first().isEnglishLetterOrDigit() // marks is `start` pointed char a letter or digit ?

        for (idx in sentence.indices) {
            val c = sentence[idx]

            val thisIsLOD = c.isEnglishLetterOrDigit()
            if (thisIsLOD xor startIsLOD) {
                res.add(sentence.substring(start, idx))
                start = idx
                startIsLOD = thisIsLOD
            }
        }

        res.add(sentence.substring(start, sentence.length))

    }

    return res
}
