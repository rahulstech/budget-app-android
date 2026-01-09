package rahulstech.android.budgetapp.budgetdb

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

object Converters {

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun toString(value: LocalDate?): String? =
        value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun toString(value: LocalDateTime?): String? =
        value?.toString()
}