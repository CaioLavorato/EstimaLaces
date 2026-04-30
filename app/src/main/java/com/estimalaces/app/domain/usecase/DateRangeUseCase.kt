package com.estimalaces.app.domain.usecase

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class DateRange(val start: Long, val end: Long)

class DateRangeUseCase(
    private val zoneId: ZoneId = ZoneId.of("America/Sao_Paulo")
) {
    fun today(): DateRange = range(LocalDate.now(zoneId), LocalDate.now(zoneId))

    fun week(): DateRange {
        val today = LocalDate.now(zoneId)
        return range(
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        )
    }

    fun month(): DateRange {
        val today = LocalDate.now(zoneId)
        return range(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()))
    }

    fun range(start: LocalDate, end: LocalDate): DateRange {
        val from = start.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val to = end.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return DateRange(from, to)
    }
}
