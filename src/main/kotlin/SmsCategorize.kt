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

    /**
     * This method reads the file from the given file path and using the regex finds the merchant,category,payment mode
     * and writes back to a new csv file named smscategory.csv in the current working directory
     * */
    suspend fun readSmsDataFromFileAndCategorize(filePath: String) {


        val smsData = withContext(Dispatchers.IO) {
            BufferedReader(FileReader(filePath))
        }

        val csvFileName = "smscategory.csv"

        try {
            // Create a FileWriter
            val csvWriter = withContext(Dispatchers.IO) {
                FileWriter(csvFileName)
            }

            val headers = "Sms,Merchant Name,Category,Payment Mode,Amount\n"
            withContext(Dispatchers.IO) {
                csvWriter.append(headers)
                smsData.readLine() //skip reading the headers
            }


            smsData.forEachLine {
                val merchantName = givenSmsDataFindMerchant(it)
                print("Merchant Name $merchantName ")
                val category = merchantCategoryMap[merchantName] ?: "Misc"
                print("Category Name $category ")
                val paymentMode = givenSmsDataFindModeOfPayment(it)
                println("Payment Mode $paymentMode ")

                val amount = givenSmsDataFindAmount(it)

                println("Amount $amount")

                csvWriter.append(
                    "$it,$merchantName,$category,$paymentMode,$amount\n"
                )
            }

            withContext(Dispatchers.IO) {
                csvWriter.flush()
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
            matcher.group(0).lowercase()
        } else {
            "Misc"
        }

    }

    private fun givenSmsDataFindMerchant(smsData: String): String {
        val regex = ".*(Paytm|Amazon|Flipkart|Myntra).*"
        val pattern: Pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(smsData)
        return if (matcher.find()) {
            matcher.group(0).lowercase()
        } else {
            "Misc"
        }
    }

    private fun givenSmsDataFindAmount(smsData: String): String? {

        val regex = "\\d+\\.\\d{2}"
        val pattern: Pattern = Pattern.compile(regex)

        val matcher = pattern.matcher(smsData)

        return if (matcher.find()) {
            matcher.group(0)
        } else {
            null
        }


    }
}