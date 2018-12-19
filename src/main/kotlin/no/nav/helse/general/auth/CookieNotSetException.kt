package no.nav.helse.general.auth

import java.lang.RuntimeException

class CookieNotSetException(cookieName : String) : RuntimeException(String.format("No cookie with name '%s' set on request", cookieName))