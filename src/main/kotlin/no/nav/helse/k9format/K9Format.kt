package no.nav.helse.k9format

import no.nav.helse.soker.Søker
import no.nav.helse.soknad.*
import no.nav.k9.søknad.felles.LovbestemtFerie
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.*
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap.BeredskapPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk.NattevåkPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.SøknadInfo
import no.nav.k9.søknad.ytelse.psb.v1.Uttak
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import java.time.Duration
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap as K9Beredskap
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk as K9Nattevåk
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning as K9Tilsynsordning

const val DAGER_PER_UKE = 5
private val k9FormatVersjon = Versjon.of("1.0")

fun Double.tilFaktiskTimerPerUke(prosent: Double) = this.times(prosent.div(100))
fun Double.tilTimerPerDag() = this.div(DAGER_PER_UKE)
fun Double.tilDuration() = Duration.ofMinutes((this * 60).toLong())

fun Søknad.tilK9Format(mottatt: ZonedDateTime, søker: Søker): K9Søknad {
    val søknadsPeriode = Periode(fraOgMed, tilOgMed)
    val søknad = K9Søknad(
        SøknadId.of("123"), //TODO Må løses, per nå settes søknadId i mottak
        k9FormatVersjon,
        mottatt,
        søker.tilK9Søker(),
        PleiepengerSyktBarn(
            søknadsPeriode,
            byggSøknadInfo(),
            barn.tilK9Barn(),
            byggK9ArbeidAktivitet(søker),
            beredskap?.tilK9Beredskap(søknadsPeriode),
            nattevåk?.tilK9Nattevåk(søknadsPeriode),
            tilsynsordning?.tilK9Tilsynsordning(søknadsPeriode),
            byggK9Arbeidstid(søker),
            byggK9Uttak(søknadsPeriode),
            ferieuttakIPerioden?.tilK9LovbestemtFerie(),
            medlemskap.tilK9Bosteder(),
            utenlandsoppholdIPerioden?.tilK9Utenlandsopphold(søknadsPeriode)
        )
    )
    return søknad

}

fun Søker.tilK9Søker(): K9Søker = K9Søker(NorskIdentitetsnummer.of(fødselsnummer))

fun BarnDetaljer.tilK9Barn(): K9Barn = K9Barn(NorskIdentitetsnummer.of(fødselsnummer), (fødselsdato))

fun Søknad.byggSøknadInfo(): SøknadInfo = SøknadInfo(
    barnRelasjon?.utskriftsvennlig ?: "Forelder",
    skalBekrefteOmsorg,
    beskrivelseOmsorgsrollen,
    harForståttRettigheterOgPlikter,
    harBekreftetOpplysninger,
    null,
    samtidigHjemme,
    harMedsøker,
    bekrefterPeriodeOver8Uker
)

fun Beredskap.tilK9Beredskap(
    periode: Periode
): K9Beredskap? = if (!beredskap) null else K9Beredskap(mapOf(periode to BeredskapPeriodeInfo(tilleggsinformasjon)))

fun Nattevåk.tilK9Nattevåk(
    periode: Periode
): K9Nattevåk? = if (harNattevåk == null || !harNattevåk) null else K9Nattevåk(mapOf(periode to NattevåkPeriodeInfo(tilleggsinformasjon)))


fun Tilsynsordning.tilK9Tilsynsordning(
    periode: Periode
): K9Tilsynsordning? = when (svar) {
    TilsynsordningSvar.ja -> K9Tilsynsordning(mutableMapOf(periode to TilsynPeriodeInfo(ja!!.snittTilsynsTimerPerDag())))
    else -> null
}

fun Søknad.byggK9Uttak(periode: Periode): Uttak? {
    return null
    /*val perioder = mutableMapOf<Periode, UttakPeriodeInfo>()

    perioder[periode] = UttakPeriodeInfo(null) //TODO Mangler info om dette i brukerdialog

    return Uttak(perioder)*/
}

fun FerieuttakIPerioden.tilK9LovbestemtFerie(): LovbestemtFerie? {
    if (!skalTaUtFerieIPerioden) return null

    val perioder = mutableListOf<Periode>()

    ferieuttak.forEach { ferieuttak ->
        perioder.add(Periode(ferieuttak.fraOgMed, ferieuttak.tilOgMed))
    }

    return LovbestemtFerie(perioder)
}

fun Medlemskap.tilK9Bosteder(): Bosteder? {
    if (utenlandsoppholdSiste12Mnd.isEmpty() && utenlandsoppholdNeste12Mnd.isEmpty()) return null

    val perioder = mutableMapOf<Periode, Bosteder.BostedPeriodeInfo>()

    utenlandsoppholdSiste12Mnd.forEach { bosted ->
        perioder[Periode(bosted.fraOgMed, bosted.tilOgMed)] = Bosteder.BostedPeriodeInfo(Landkode.of(bosted.landkode))
    }

    utenlandsoppholdNeste12Mnd.forEach { bosted ->
        perioder[Periode(bosted.fraOgMed, bosted.tilOgMed)] = Bosteder.BostedPeriodeInfo(Landkode.of(bosted.landkode))
    }

    return Bosteder(perioder)
}

private fun UtenlandsoppholdIPerioden.tilK9Utenlandsopphold(
    periode: Periode
): Utenlandsopphold? {
    if (opphold.isEmpty()) return null

    val perioder = mutableMapOf<Periode, UtenlandsoppholdPeriodeInfo>()

    opphold.forEach {
        val årsak = when (it.årsak) {
            Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING -> UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
            Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD -> UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
            else -> null
        }

        perioder[periode] = UtenlandsoppholdPeriodeInfo.builder()
            .land(Landkode.of(it.landkode))
            .årsak(årsak)
            .build()
    }

    return Utenlandsopphold(perioder)
}