package no.nav.pleiepenger.api.general.validation

import java.lang.RuntimeException

class ValidationException(
    val violations : List<Violation>
) : RuntimeException("Invalid Request")
