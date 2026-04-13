package serie1.problema

import java.text.SimpleDateFormat
import java.util.*

/*fun dateStringToSeconds(dateString: String): Long {
    // Define the date format of your input string
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    // Set the timezone if necessary
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    try {
        // Parse the date string into a Date object
        val date = dateFormat.parse(dateString)
        // Calculate the number of milliseconds since the epoch (January 1, 1970, 00:00:00 UTC)
        val milliseconds = date.time
        // Convert milliseconds to seconds
        return milliseconds / 1000
    } catch (e: Exception) {
        // Handle parsing exceptions, if any
        e.printStackTrace()
    }
    // Return -1 if parsing fails
    return -1
}*/

fun main() {
    //val dateString = "2024-03-08 10:30:00"
    var dateString = "2014-06-30 02:11:03"
    var seconds = dateStringToSeconds(dateString)
    println("Seconds since epoch for '$dateString': $seconds")

    dateString = "2014-07-30 02:11:03"
    seconds = dateStringToSeconds(dateString)
    println("Seconds since epoch for '$dateString': $seconds")
}