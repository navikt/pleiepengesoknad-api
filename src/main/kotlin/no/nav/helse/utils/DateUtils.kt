package no.nav.helse.utils

import java.time.LocalDate

fun LocalDate.erLikEllerEtterDagensDato() = isEqual(LocalDate.now()) || isAfter(LocalDate.now())
