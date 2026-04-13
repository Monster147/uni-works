package serie1.problem

import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter

fun createReader(fileName: String): BufferedReader {
    return BufferedReader(FileReader(fileName))
}

fun createWriter(fileName: String?): PrintWriter {
    return PrintWriter(fileName)
}

/** Usage Example
 *  Read a file that contains *tweets* and write the
 *  field hashtags into another file.
 * **/
fun main(args: Array<String>) {
    val fr = createReader("tweetsMinSample.twt")
    val pw = createWriter("hashTags.txt")

    // Read files and write into output.txt
    var line = fr.readLine()
    while (line != null) {
       val fields = line.split(";") // obtain the fields of the tweet
       for ( field in fields ) {
           if (field.contains("hashtags"))
                pw.println(field)
       }
       line = fr.readLine()
    }
    fr.close()
    pw.close()
}