package no.nav.helse.soknad

import javax.validation.constraints.Size

private const val MAX_VEDLEGG_SIZE = 8 * 1024 * 1024

data class Vedlegg(@get:Size(min = 1, max= MAX_VEDLEGG_SIZE ) val innhold: ByteArray)