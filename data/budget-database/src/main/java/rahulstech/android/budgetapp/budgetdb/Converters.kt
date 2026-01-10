package rahulstech.android.budgetapp.budgetdb

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

object Converters {

    @TypeConverter
    fun localDateToLong(value: LocalDate?): Long? =
        value
            ?.atStartOfDay()
            ?.atZone(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()

    @TypeConverter
    fun longToLocalDate(value: Long?): LocalDate? =
        value?.let { epochMillis ->
            Instant.ofEpochMilli(epochMillis)
                // apply the timezone offset on the epochMillis
                // ex: if offset is UTC+05:30 then output will be
                // epochMillis + (5 * 3600 + 30 * 60) * 1000
                .atZone(ZoneOffset.systemDefault())
                .toLocalDate()
        }

}