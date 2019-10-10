package no.nav.helse.arbeidsgiver

data class Arbeidsgivere (
    val organisasjoner: List<Organisasjon>
)

class Organisasjon (
    val organisasjonsnummer: String,
    val navn: String?
)