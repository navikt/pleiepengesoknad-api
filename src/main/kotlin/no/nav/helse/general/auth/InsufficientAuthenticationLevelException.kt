package no.nav.helse.general.auth

class InsufficientAuthenticationLevelException(actualAcr : String, requiredAcr : String) : RuntimeException("Innloggingen er på nivå '$actualAcr'. Denne tjenesten krever '$requiredAcr'")