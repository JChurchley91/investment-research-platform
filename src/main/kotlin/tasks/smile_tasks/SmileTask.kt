package tasks.smile_tasks

import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import smile.nlp.normalize
import java.text.Normalizer
import java.time.LocalDate

/**
 * Base class for Smile Tasks.
 * This class provides common functionality for tasks
 * that use the smile-kotlin library.
 *
 * @property taskName The name of the task.
 * @property taskSchedule The schedule of the task.
 */
open class SmileTask(
    val taskName: String,
    val taskSchedule: String,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val today: LocalDate = LocalDate.now().minusDays(2)

    fun cleanTextForNlp(htmlText: String): String {
        // Step 1: Parse HTML and extract text
        val plainText = Jsoup.parse(htmlText).text()

        // Step 2: Normalize text to remove accent marks
        val normalizedText =
            Normalizer
                .normalize(plainText, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

        // Step 3: Remove numbers and numerical patterns like percentages or dollar amounts
        val withoutNumbers =
            normalizedText
                // Remove percentage patterns (e.g., 3.12%)
                .replace("\\d+(\\.\\d+)?%".toRegex(), "")
                // Remove dollar amounts (e.g., $12.34)
                .replace("\\$\\d+(\\.\\d+)?".toRegex(), "")
                // Remove standalone numbers (e.g., 7.73 or 163.35)
                .replace("\\b\\d+(\\.\\d+)?\\b".toRegex(), "")

        // Step 4: Remove excessive line breaks and whitespace
        val cleanedText =
            withoutNumbers
                .replace("\n+".toRegex(), " ") // Replace multiple line breaks with a single space
                .replace("\\s+".toRegex(), " ") // Collapse multiple spaces into a single space
                .trim() // Remove extra spaces at the start and end

        return cleanedText
            .replace("\n".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
            .normalize()
            .replace("\"", "")
            .replace("\\", "")
            .replace("%\n%", " ")
    }
}
