/*

    程序进入点

    自动分词
    《中文信息处理》大作业

    2051565 GTY

*/


/**
 * 交互式操作界面。
 *
 * 指令说明：
 *     exit    : 退出shell
 *     count·X : 统计“X”在词典内登记的次数
 *     X       : 对语句X进行切分
 */
fun shell(model: NGramModel) {
    while (true) {
        print("输入待分割的句子：")
        val line = readln()

        if (line == "exit") { // 如果输入 exit，就退出。

            println("bye!")
            break

        } else if (line.startsWith("count·")) { // 特殊命令：统计一个词在字典内登记的次数。

            val str = line.split("·")[1] // 假设句子里不允许出现“·”。因此，输入的指令会被分割成两段，第二段是要查的词。
            println("count: $str : ${model.getCount(str)}")

        } else {

            // 尝试分词，并输出结果。
            // 输出方式：在不同词之间插入“·”
            // 如：同济大学 -> 同济·大学

            model.process(line).forEachIndexed { idx, it ->

                // process 方法返回一个分词结果列表。
                // 对其进行 forEach 循环，idx 是当前循环到的数组下标，it 是这个词本身。

                if (idx > 0) {
                    // 如果当前输出的不是第一个词，那么要在输出这个词之前，先输出一个分割符号。
                    print("·")
                }

                print(it)
            }

            println()

        }
    } // end of while (true)
} // end of fun shell(model: NGramModel)

fun main() {

    // 读入文件里的句子（新闻），并预处理。
    val sentences = readSentences("data/newsdata.txt", 2000).removeStrangeTokens()

    // 声明模型对象。
    val model = NGramModel()

    // 训练模型。
    model.train(sentences)

    // 启动用户交互界面。
    shell(model)

}
