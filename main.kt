/*

    程序进入点

    自动分词
    《中文信息处理》大作业

    2051565 GTY

*/


/**
 * 交互式操作界面。
 */
fun shell(model: NGramModel) {
    while (true) {
        print("输入待分割的句子：")
        val line = readln()

        if (line == "exit") { // 如果输入 exit，就退出。

            println("bye!")
            break

        } else if (line.startsWith("count·")) { // 特殊命令：统计一个词在字典内登记的次数。

            val str = line.split("·")[1]
            println("count: $str : ${model.getCount(str)}")

        } else {

            // 尝试分词，并输出结果。

            model.process(line).forEachIndexed { idx, it ->
                if (idx > 0) {
                    print("·")
                }
                print(it)
            }

            println()

        }
    }
}

fun main() {

    // 读入文件，并预处理。
    val sentences = readSentences("data/newsdata.txt", 2000).removeStrangeTokens()

    val model = NGramModel()

    // 训练模型。
    model.train(sentences)

    // 启动用户交互界面。
    shell(model)

}
