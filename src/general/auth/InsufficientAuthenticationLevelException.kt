package no.nav.pleiepenger.api.general.auth

import java.lang.RuntimeException

class InsufficientAuthenticationLevelException(acr : String) : RuntimeException(String.format("Requires authentication Level4, was '%s'", acr))