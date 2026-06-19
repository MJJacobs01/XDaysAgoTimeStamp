package za.co.jacobs.mj.xdaysagotimestamp

import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [convertLongTimeToDateAndTimeStamp].
 *
 * Both `now` and `timeZone` are injected so the output is fully deterministic.
 * BASE renders, in UTC, as 2023/11/14 at 22:13.
 */
class TimeStampUtilTest {

    private val utc = TimeZone.UTC
    private val base = 1_700_000_000_000L // 2023-11-14T22:13:20Z

    private val second = 1_000L
    private val minute = 60_000L
    private val hour = 3_600_000L
    private val day = 86_400_000L

    private fun convert(timeStamp: Long, now: Long) =
        convertLongTimeToDateAndTimeStamp(timeStamp, now = now, timeZone = utc)

    @Test
    fun zeroDifference_isZeroSecondsAgo() {
        assertEquals("0 seconds ago", convert(base, now = base))
    }

    @Test
    fun withinAMinute_isSecondsAgo() {
        assertEquals("59 seconds ago", convert(base, now = base + 59 * second))
    }

    @Test
    fun sixtySecondBoundary_isOneMinuteAgo() {
        assertEquals("1 minutes ago", convert(base, now = base + minute))
    }

    @Test
    fun withinAnHour_isMinutesAgo() {
        assertEquals("5 minutes ago", convert(base, now = base + 5 * minute))
    }

    @Test
    fun sixtyMinuteBoundary_isOneHourAgo() {
        assertEquals("1 hours ago", convert(base, now = base + hour))
    }

    @Test
    fun withinADay_isHoursAgo() {
        assertEquals("5 hours ago", convert(base, now = base + 5 * hour))
    }

    @Test
    fun twentyFourHourBoundary_isOneDayAgoWithTime() {
        assertEquals("1 day ago at 22:13", convert(base, now = base + day))
    }

    @Test
    fun multipleDays_arePluralWithTime() {
        assertEquals("5 days ago at 22:13", convert(base, now = base + 5 * day))
    }

    @Test
    fun tenDayBoundary_isAbsoluteDate() {
        assertEquals("2023/11/14 at 22:13", convert(base, now = base + 10 * day))
    }

    @Test
    fun olderThanTenDays_isAbsoluteDate() {
        assertEquals("2023/11/14 at 22:13", convert(base, now = base + 30 * day))
    }

    @Test
    fun futureTimestamp_isAbsoluteDate() {
        // Regression guard: this used to fall through to null -> "No Time given".
        assertEquals("2023/11/14 at 22:13", convert(base, now = base - second))
    }
}
