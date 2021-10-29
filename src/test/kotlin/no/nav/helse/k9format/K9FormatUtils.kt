package no.nav.helse.k9format

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Søker
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

fun defaultK9FormatPSB(
    søknadId: UUID = UUID.randomUUID(), søknadsPeriode:
    Periode = Periode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-01"))
) = Søknad(

    SøknadId.of(søknadId.toString()),
    Versjon.of("1.0.0"),
    ZonedDateTime.parse("2020-01-01T10:00:00Z"),
    Søker(NorskIdentitetsnummer.of("12345678910")),
    PleiepengerSyktBarn()
        .medSøknadsperiode(søknadsPeriode)
        .medSøknadInfo(DataBruktTilUtledning(true, true, true, true, true))
        .medBarn(Barn(NorskIdentitetsnummer.of("10987654321"), null))
        .medTilsynsordning(
            Tilsynsordning().medPerioder(
                mapOf(
                    Periode(
                        LocalDate.parse("2020-01-01"),
                        LocalDate.parse("2020-01-05")
                    ) to TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(Duration.ofHours(8)),
                    Periode(
                        LocalDate.parse("2020-01-06"),
                        LocalDate.parse("2020-01-10")
                    ) to TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(Duration.ofHours(4))
                )
            )
        )
        .medArbeidstid(
            Arbeidstid().medArbeidstaker(
                listOf(
                    Arbeidstaker(
                        NorskIdentitetsnummer.of("12345678910"),
                        Organisasjonsnummer.of("926032925"),
                        ArbeidstidInfo(
                            mapOf(
                                Periode(
                                    LocalDate.parse("2018-01-01"),
                                    LocalDate.parse("2020-01-05")
                                ) to ArbeidstidPeriodeInfo(Duration.ofHours(8), Duration.ofHours(4)),
                                Periode(
                                    LocalDate.parse("2020-01-06"),
                                    LocalDate.parse("2020-01-10")
                                ) to ArbeidstidPeriodeInfo(Duration.ofHours(8), Duration.ofHours(2))
                            )
                        )
                    )
                )
            )
        )
)
