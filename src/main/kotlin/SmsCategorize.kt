import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import java.util.regex.Pattern


class SmsCategorize {

    private val merchantCategoryMap = mapOf(
        "paytm" to "wallet",
        "amazon" to "shopping",
        "flipkart" to "shopping",
        "myntra" to "shopping"
    )

    suspend fun readSmsDataFromFileAndCategorize(filePath: String) {


        val smsData = withContext(Dispatchers.IO) {
            BufferedReader(FileReader(filePath))
        }

        val csvFileName = "smscategory.csv"

        try {
            // Create a FileWriter and CSVPrinter with the desired format
            val csvWriter = withContext(Dispatchers.IO) {
                FileWriter(csvFileName)
            }

            withContext(Dispatchers.IO) {
                csvWriter.append("Sms")
                csvWriter.append(",")
                csvWriter.append("Merchant Name")
                csvWriter.append(",")
                csvWriter.append("Category")
                csvWriter.append(",")
                csvWriter.append("Payment Mode")
                csvWriter.append("\n")

            }


            withContext(Dispatchers.IO) {
                smsData.readLine()
            }

            smsData.forEachLine {
                val merchantName = givenSmsDataFindMerchant(it)
                print("Merchant Name $merchantName ")
                val category = merchantCategoryMap[merchantName] ?: "Misc"
                print("Category Name $category ")
                val paymentMode = givenSmsDataFindModeOfPayment(it)
                println("Payment Mode $paymentMode ")

                csvWriter.append(
                    "".plus(it).plus(",").plus(merchantName).plus(",").plus(category).plus(",").plus(paymentMode)
                        .plus("\n")
                )
            }

            withContext(Dispatchers.IO) {
                csvWriter.flush()
            }
            withContext(Dispatchers.IO) {
                csvWriter.close()
            }


        } catch (ex: Exception) {
            print("Exception occurred")
            println(ex.stackTraceToString())

        }
    }


    private fun givenSmsDataFindModeOfPayment(smsData: String): String {

        val regex = "\\b(Paytm|Google Pay|PhonePe|UPI|Netbanking|Credit Card|Debit Card|Wallet|Cash|NEFT|IMPS)\\b"

        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(smsData)
        return if (matcher.find()) {
            matcher.group(1).lowercase(Locale.getDefault())
        } else {
            "Misc"
        }

    }

    private fun givenSmsDataFindMerchant(smsData: String): String {
        val regex = ".*(Paytm|Amazon|Flipkart|Myntra).*"
        val pattern: Pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(smsData)
        return if (matcher.find()) {
            matcher.group(1).lowercase(Locale.getDefault())
        } else {
            "Misc"
        }
    }
}