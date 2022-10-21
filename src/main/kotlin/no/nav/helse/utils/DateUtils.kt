package no.nav.helse.utils

import java.time.DayOfWeek
import java.time.LocalDate

fun LocalDate.erLikEllerEtterDagensDato() = isEqual(LocalDate.now()) || isAfter(LocalDate.now())
fun LocalDate.erFørDagensDato() = isBefore(LocalDate.now())

fun LocalDate.ikkeErHelg(): Boolean = dayOfWeek != DayOfWeek.SUNDAY && dayOfWeek != DayOfWeek.SATURDAY
