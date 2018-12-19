package no.nav.helse.general.validation

import java.lang.RuntimeException

class ValidationException(
    val violations : List<Violation>
) : RuntimeException("Invalid Request")
