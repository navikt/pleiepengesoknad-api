package no.nav.helse.general.auth

import java.lang.RuntimeException

class CookieNotSetException(cookieName : String) : RuntimeException("Ingen cookie med navnet '$cookieName' satt.")