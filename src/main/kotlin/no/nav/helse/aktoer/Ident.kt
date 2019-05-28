package no.nav.helse.aktoer

import no.nav.helse.dusseldorf.ktor.core.erGyldigFodselsnummer

data class AktoerId(val value: String)

fun String.tilNorskIdent() = if (erGyldigFodselsnummer()) Fodselsnummer(this) else AlternativId(this)

interface NorskIdent{
    fun getValue() : String
}

data class Fodselsnummer(private val value: String) : NorskIdent {
    override fun getValue() = value
}
data class AlternativId(private val value: String) : NorskIdent {
    override fun getValue() = value
}