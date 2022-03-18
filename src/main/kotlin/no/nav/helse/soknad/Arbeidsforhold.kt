package no.nav.helse.soknad

data class Arbeidsforhold(
    val jobberNormaltTimer: Double,
    val harFraværIPeriode: Boolean,
    val arbeidIPeriode: ArbeidIPeriode? = null
)

data class ArbeidIPeriode(
    val jobberIPerioden: JobberIPeriodeSvar,
    val jobberProsent: Double? = null,
    val erLiktHverUke: Boolean? = null,
    val enkeltdager: List<Enkeltdag>? = null,
    val fasteDager: PlanUkedager? = null
)

enum class JobberIPeriodeSvar {
    JA,
    NEI
}