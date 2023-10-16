suspend fun main(args: Array<String>) {

    val filePath = args[0]


    val smsCategorize = SmsCategorize()

    smsCategorize.readSmsDataFromFileAndCategorize(filePath)
}