package za.co.jacobs.mj.xdaysagotimestamp

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Created by MJ Jacobs on 2023/11/19 at 09:40
 */

private const val SIXTY_SECONDS_MILLIS = 60_000L
private const val SIXTY_MINUTES_MILLIS = 3_600_000L
private const val TWENTY_FOUR_HOURS_MILLIS = 86_400_000L
private const val TEN_DAYS_MILLIS = 864_000_000L

/**
 * Converts an epoch-milliseconds timestamp into a human-readable, relative time string.
 *
 * - within the last minute -> "N seconds ago"
 * - within the last hour   -> "N minutes ago"
 * - within the last day    -> "N hours ago"
 * - within the last 10 days-> "N day(s) ago at HH:mm"
 * - older than 10 days, or in the future -> "yyyy/MM/dd at HH:mm" (absolute)
 *
 * @param timeStamp the instant to describe, in epoch milliseconds.
 * @param now the reference "current" instant in epoch milliseconds. Defaults to the
 *   system clock; injectable so the result is deterministic and unit-testable.
 * @param timeZone the time zone used to render absolute dates/times. Defaults to the
 *   device's current zone; injectable for deterministic tests.
 */
@OptIn(ExperimentalTime::class)
fun convertLongTimeToDateAndTimeStamp(
    timeStamp: Long,
    now: Long = Clock.System.now().toEpochMilliseconds(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val difference = now - timeStamp

    return when {
        // Future timestamps, or anything older than 10 days, render as an absolute date.
        difference !in 0L until TEN_DAYS_MILLIS -> formatAbsolute(timeStamp, timeZone)

        difference < SIXTY_SECONDS_MILLIS -> {
            val seconds = difference / 1000
            "$seconds seconds ago"
        }

        difference < SIXTY_MINUTES_MILLIS -> {
            val minutes = difference / SIXTY_SECONDS_MILLIS
            "$minutes minutes ago"
        }

        difference < TWENTY_FOUR_HOURS_MILLIS -> {
            val hours = difference / SIXTY_MINUTES_MILLIS
            "$hours hours ago"
        }

        else -> { // 24 hours up to (but not including) 10 days
            val daysAgo = difference / TWENTY_FOUR_HOURS_MILLIS
            val timeString = formatTime(timeStamp, timeZone)
            if (daysAgo == 1L) "$daysAgo day ago at $timeString" else "$daysAgo days ago at $timeString"
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun formatTime(timeStamp: Long, timeZone: TimeZone): String {
    val dateTime = Instant.fromEpochMilliseconds(timeStamp).toLocalDateTime(timeZone)
    return "%02d:%02d".format(dateTime.hour, dateTime.minute)
}

@OptIn(ExperimentalTime::class)
private fun formatAbsolute(timeStamp: Long, timeZone: TimeZone): String {
    val dateTime = Instant.fromEpochMilliseconds(timeStamp).toLocalDateTime(timeZone)
    val date = "%04d/%02d/%02d".format(dateTime.year, dateTime.month.number, dateTime.day)
    val time = "%02d:%02d".format(dateTime.hour, dateTime.minute)
    return "$date at $time"
}
