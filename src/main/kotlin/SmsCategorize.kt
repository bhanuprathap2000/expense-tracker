import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.util.regex.Pattern


class SmsCategorize {

    private val merchantCategoryMap = mapOf(
        "paytm" to "wallet",
        "amazon" to "shopping",
        "flipkart" to "shopping",
        "myntra" to "shopping",
        "act broadband" to "bills",
        "airtel" to "bills",
        "jio" to "bills",
        "bookmyshow" to "entertainment",
        "bsnl" to "bills",
        "cleartrip" to "travel",
        "goibibo" to "travel",
        "irctc" to "travel",
        "makemytrip" to "travel",
        "olacabs" to "travel",
        "netflix" to "entertainment",
        "hotstar" to "entertainment",
        "amazon prime" to "entertainment"

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
                val category = merchantCategoryMap[merchantName] ?: "Misc"
                val paymentMode = givenSmsDataFindModeOfPayment(it)

                val amount = givenSmsDataFindAmount(it)

                csvWriter.append(
                    "$it,$merchantName,$category,$paymentMode,$amount\n"
                )
            }

            withContext(Dispatchers.IO) {
                csvWriter.flush()
                csvWriter.close()
            }

            println("Process completed,Please find the smscategory.csv in your working directory")


        } catch (ex: Exception) {
            print("Exception occurred")
            println(ex.stackTraceToString())

        }
    }


    private fun givenSmsDataFindModeOfPayment(smsData: String): String {

        val regex = "\\b(Paytm|Google Pay|PhonePe|UPI|NetBanking|Cash Deposit|ATM|Credit Card|Debit Card|Wallet|Cash|NEFT|IMPS)\\b"

        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(smsData)
        return if (matcher.find()) {
            matcher.group(0).lowercase()
        } else {
            "Misc"
        }

    }

    private fun givenSmsDataFindMerchant(smsData: String): String {
        val regex = ".*(Paytm|Amazon|Flipkart|Myntra|ACT Broadband|Airtel|Jio|BookMyShow|BSNL|Cleartrip|GoIbibo|IRCTC|MakeMyTrip|Olacabs|Netflix|Hotstar|Amazon prime).*"
        val pattern: Pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(smsData)
        return if (matcher.find()) {
            matcher.group(0).lowercase()
        } else {
            "Misc"
        }
    }

    private fun givenSmsDataFindAmount(smsData: String): String? {

        val amountRegex = "(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)"
        val numberRegex = "\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?"
        val amountPattern: Pattern = Pattern.compile(amountRegex)
        val numberPattern = Pattern.compile(numberRegex)

        val matcher = amountPattern.matcher(smsData)



        return if (matcher.find()) {
            val amount: String = matcher.group(0)
            val numberMatcher = numberPattern.matcher(amount)

            if (numberMatcher.find()) numberMatcher.group(0) else null
        } else {
            null
        }


    }
}