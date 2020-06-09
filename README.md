# pleiepengesoknad-api

API for Selvbetjeningsløsning for søknad om pleiepenger

![CI / CD](https://github.com/navikt/pleiepengesoknad-api/workflows/CI%20/%20CD/badge.svg)
![NAIS Alerts](https://github.com/navikt/pleiepengesoknad-api/workflows/Alerts/badge.svg)

Benyttet av [pleiepengesoknad](https://github.com/navikt/pleiepengesoknad)

## Endepunkt

### Sende inn søknad

POST @ /soknad -> 202 Response

- språk er en valgfri attributt. Om den settes må den være enten "nb" for Bokmål eller "nn" for Nynorsk.
- Listen med arbeidsgivere inneholder data på samme format som GET @ /arbeidsgiver, med to valgfrie attributter (om en er satt må begge settes);
- arbeidsgivere.organisasjoner[x].skalJobbeProsent er valgfri. Om satt må den være mellom 0.0 og 100.0. Om 'grad' ikke er satt må denne alltid settes.
- Listen med organisajoner i arbeidsgivere kan være tom.
- Vedlegg er en liste med URL'er som peker tilbake på 'Location' headeren returnert i opplasting av vedlegg
- Det må sendes med minst ett vedlegg
- Det kan settes kun 1 ID på barnet (alternativId, aktørId eller fødselsnummer) - Kan også sendes uten ID
- barn.alternativId må være 11 siffer om det er satt
- barn.fødslsnummer må være et gyldig norsk fødselsnummer om den er satt
- barn.aktørId må være sattil en gyldig Aktør ID om den er satt
- barn.navn er kun påkrevd om 'barn.fødselsnummer' er satt
- relasjonTilBarnet er ikke påkrevd om 'barn.aktørId' er satt, ellers påkrevd
- grad er valgfri. Om satt må den være mellom 20 og 100
- 'harBekreftetOpplysninger' og 'harForståttRettigheterOgPlikter' må være true
- 'dagerPerUkeBorteFraJobb' er valgfri. Om satt må den være mellom 0.5 og 5.0. Om 'grad' ikke er satt og 'harMedsøker' er true må den settes.
- tilsynsordning er ikke påkrevd, se eget avsnitt under om det er satt.
- beredskap er ikke påkrevd, men om det er satt må 'beredskap' være satt og 'tilleggsinformasjon' være maks 1000 tegn om den er satt.
- nattevåk er ikke påkrevd, men om det er satt må 'harNattevåk' være satt og 'tilleggsinformasjon' være maks 1000 tegn om den er satt.

```json
{
  "newVersion": null,
  "språk": "nb",
  "barn": {
    "fødselsnummer": "123456789",
    "navn": "Barn Barnesen",
    "aktørId": "12345",
    "fødselsdato": "2018-01-01"
  },
  "relasjonTilBarnet": "mor",
  "fraOgMed": "2020-01-01",
  "tilOgMed": "2020-01-10",
  "samtidigHjemme": true,
  "arbeidsgivere": {
    "organisasjoner": [
      {
        "organisasjonsnummer": "917755736",
        "navn": "Org",
        "skalJobbeProsent": 10,
        "jobberNormaltTimer": 10,
        "skalJobbe": "redusert",
        "vetIkkeEkstrainfo": null
      }
    ]
  },
  "vedlegg": [
    "http://localhost:8080/vedlegg/1"
  ],
  "medlemskap": {
    "harBoddIUtlandetSiste12Mnd": true,
    "skalBoIUtlandetNeste12Mnd": true,
    "utenlandsoppholdNeste12Mnd": [
      {
        "fraOgMed": "2018-01-01",
        "tilOgMed": "2018-01-10",
        "landkode": "DEU",
        "landnavn": "Tyskland"
      }
    ],
    "utenlandsoppholdSiste12Mnd": [
      {
        "fraOgMed": "2017-01-01",
        "tilOgMed": "2017-01-10",
        "landkode": "DEU",
        "landnavn": "Tyskland"
      }
    ]
  },
  "selvstendigVirksomheter": [
    {
      "næringstyper": [
        "ANNEN"
      ],
      "fiskerErPåBladB": false,
      "organisasjonsnummer": null,
      "fraOgMed": "2020-01-01",
      "tilOgMed": null,
      "næringsinntekt": 1111,
      "navnPåVirksomheten": "TullOgTøys",
      "registrertINorge": false,
      "registrertIUtlandet": {
        "landnavn": "Tyskland",
        "landkode": "DEU"
      },
      "varigEndring": {
        "inntektEtterEndring": 9999,
        "dato": "2020-01-01",
        "forklaring": "Korona"
      },
      "regnskapsfører": {
        "navn": "Kjell Regnskap",
        "telefon": "123456789"
      },
      "revisor": null,
      "yrkesaktivSisteTreFerdigliknedeÅrene": {
        "oppstartsdato": "2018-01-01"
      }
    }
  ],
  "utenlandsoppholdIPerioden": {
    "skalOppholdeSegIUtlandetIPerioden": true,
    "opphold": [
      {
        "fraOgMed": "2019-10-10",
        "tilOgMed": "2019-20-10",
        "landkode": "SE",
        "landnavn": "Sverige",
        "erUtenforEøs": false,
        "erBarnetInnlagt": true,
        "barnInnlagtPerioder": [
          {
            fom: "2019-11-10",
            tom: "2019-14-10"
          }
        ];
        "årsak": "ANNET"
      },
      {
        "landnavn": "Sverige",
        "landkode": "SE",
        "fraOgMed": "2019-10-10",
        "tilOgMed": "2019-11-10",
        "erUtenforEøs": false,
        "erBarnetInnlagt": true,
        "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
      },
      {
        "landnavn": "Sverige",
        "landkode": "SE",
        "fraOgMed": "2019-10-10",
        "tilOgMed": "2019-11-10",
        "erUtenforEøs": false,
        "erBarnetInnlagt": true,
        "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD"
      },
      {
        "landnavn": "Sverige",
        "landkode": "SE",
        "fraOgMed": "2019-10-10",
        "tilOgMed": "2019-11-10",
        "erUtenforEøs": false,
        "erBarnetInnlagt": false,
        "årsak": null
      }
    ]
  },
  "harMedsøker": true,
  "harBekreftetOpplysninger": true,
  "harForståttRettigheterOgPlikter": true,
  "ferieuttakIPerioden": {
    "skalTaUtFerieIPerioden": false,
    "ferieuttak": [
      {
        "fraOgMed": "2020-01-05",
        "tilOgMed": "2020-01-07"
      }
    ]
  },
  "skalBekrefteOmsorg": true,
  "skalPassePåBarnetIHelePerioden": true,
  "beskrivelseOmsorgsrollen": "En kort beskrivelse",
  "bekrefterPeriodeOver8Uker": true,
  "beredskap": {
    "beredskap": true,
    "tilleggsinformasjon": "Ikke beredskap"
  },
  "frilans": {
    "jobberFortsattSomFrilans": true,
    "startdato": "2018-01-01"
  },
  "nattevåk": {
    "harNattevåk": true,
    "tilleggsinformasjon": "Har nattevåk"
  },
  "tilsynsordning": {
    "svar": "ja",
    "ja": {
      "mandag": "PT1H",
      "tirsdag": "PT1H",
      "onsdag": "PT1H",
      "torsdag": "PT1H",
      "fredag": "PT1H",
      "tilleggsinformasjon": "Blabla"
    },
    "vetIkke": {
      "svar": "annet",
      "annet": "Nei"
    }
  }
}
```

#### Utenlandsopphold i perioden

- Attributten `skalOppholdeSegIUtlandetIPerioden` være satt. `true|false`.
- Attributten `opphold` inneholder to typer data

###### Opphold i EØS land

```json
{
  "landnavn": "Sverige",
  "landkode": "SE",
  "fraOgMed": "2020-01-01",
  "tilOgMed": "2020-02-01"
}
```

###### Opphold i et land utenfor EØS

```json
{
  "landnavn": "USA",
  "landkode": "US",
  "fraOgMed": "2020-01-08",
  "tilOgMed": "2020-01-09",
  "erUtenforEøs": true,
  "erBarnetInnlagt": true,
  "årsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
}
```

- Attributten `erUtenforEøs` er satt til `true`
- Attributten `erBarnetInnlagt` er satt til `true|false`
- Attributten `årsak` settes når `erBarnetInnlagt` er `true`. Gyldige verdier er:
  -- `BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING`
  -- `BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD`
  -- `ANNET`
- Attributten `årsak` settes til `null` når `erBarnetInnlagt` er `false`

#### Tilsynsordning

##### Ja

- Attributten `ja` være satt.
- Attributten `vetIkke` kan ikke være satt.
- Minst en av dagene må være satt (Se format på duration lengre ned)
- `ja.tilleggsinformasjon` er ikke påkrevd. Om den er satt må den være maks 140 tegn.

```json
{
  "svar": "ja",
  "ja": {
    "mandag": "PT7H30M",
    "tirsdag": null,
    "onsdag": "PT7H25M",
    "torsdag": null,
    "fredag": "PT0S",
    "tilleggsinformasjon": "Unntatt uke 37. Da er han hjemme hele tiden."
  }
}
```

##### Nei

- Hverken attributten `ja` eller `vetIkke` kan være satt.

```json
{
  "svar": "nei"
}
```

##### Vet ikke

- Attributten `vetIkke` være satt.
- Attributten `ja` kan ikke være satt.
- `vetIkke.svar` kan være enten `er_sporadisk`, `er_ikke_laget_en_plan` eller `annet`
- Om `vetIkke.svar` er `annet` må også attributten `vetIkke.svar.annet` være satt, men være maks 140 tegn.

```json
{
  "svar": "vetIkke",
  "vetIkke": {
    "svar": "annet",
    "annet": "Blir for vanskelig å si nå."
  }
}
```

### Frilanser

### Del av søknadsobjekt

```typescript
{
    harHattInntektSomFrilanser?: boolean;
    frilans?: FrilansApiData;
}
```

#### Interface for api-data

```typescript
export interface FrilansApiData {
  startdato: ApiStringDate;
  jobberFortsattSomFrilans: boolean;
}
```

### Selvstendig næringsdrivende

#### Del av søknadsobjekt

```typescript
{
    har_hatt_inntekt_som_selvstendig_naringsdrivende?: boolean;
    selvstendig_virksomheter?: VirksomhetApiData[];
}
```

#### Interface for api-data

```typescript
export declare enum Næringstype {
  "FISKER" = "FISKE",
  "JORDBRUK" = "JORDBRUK_SKOGBRUK",
  "DAGMAMMA" = "DAGMAMMA",
  "ANNEN" = "ANNEN"
}

export interface VirksomhetApiData {
  naringstype: Næringstype[];
  fiskerErPaBladB?: boolean;
  fraOgMed: ApiStringDate;
  tilOgMed?: ApiStringDate | null;
  erPagaende?: boolean;
  naringsinntekt: number;
  navn_pa_virksomheten: string;
  organisasjonsnummer?: string;
  registrert_i_norge: boolean;
  registrert_i_land?: string;
  har_blitt_yrkesaktiv_siste_tre_ferdigliknede_arene?: boolean;
  yrkesaktiv_siste_tre_ferdigliknede_arene?: {
    oppstartsdato: ApiStringDate;
  };
  har_varig_endring_av_inntekt_siste_4_kalenderar?: boolean;
  varig_endring?: {
    dato?: ApiStringDate | null;
    inntekt_etter_endring?: number;
    forklaring?: string;
  };
  har_regnskapsforer: boolean;
  regnskapsforer?: {
    navn: string;
    telefon: string;
  };
  har_revisor?: boolean;
  revisor?: {
    navn: string;
    telefon: string;
    kan_innhente_opplysninger?: boolean;
  };
}
```

### Feilmeldinger

```json
{
  "type": "/problem-details/attachments-too-large",
  "title": "attachments-too-large",
  "status": 413,
  "detail": "Totale størreslsen på alle vedlegg overstiger maks på 24 MB.",
  "instance": "about:blank"
}
```

```json
{
  "type": "/problem-details/unauthorized",
  "title": "unauthorized",
  "status": 403,
  "detail": "Søkeren er ikke myndig og kan ikke sende inn søknaden.",
  "instance": "about:blank"
}
```

### Søker

GET @/soker -> 200 Response

```json
{
  "etternavn": "MORSEN",
  "fornavn": "MOR",
  "mellomnavn": "HEISANN",
  "fødselsnummer": "290990123456",
  "fødseldato": "1997-10-05",
  "myndig": true
}
```

### Vedlegg

#### Laste opp

POST @/vedlegg -> 201 Response med 'Location' header satt til vedlegget

##### Feilmeldinger

```json
{
  "type": "/problem-details/multipart-form-required",
  "title": "multipart-form-required",
  "status": 400,
  "detail": "Requesten må være en 'multipart/form-data' request hvor en 'part' er en fil, har 'name=vedlegg' og har Content-Type header satt.",
  "instance": "about:blank"
}
```

```json
{
  "type": "/problem-details/attachment-not-attached",
  "title": "attachment-not-attached",
  "status": 400,
  "detail": "Fant ingen 'part' som er en fil, har 'name=vedlegg' og har Content-Type header satt.",
  "instance": "about:blank"
}
```

```json
{
  "type": "/problem-details/attachment-too-large",
  "title": "attachment-too-large",
  "status": 413,
  "detail": "vedlegget var over maks tillatt størrelse på 8MB.",
  "instance": "about:blank"
}
```

```json
{
  "type": "/problem-details/attachment-content-type-not-supported",
  "title": "attachment-content-type-not-supported",
  "status": 400,
  "detail": "Vedleggets type må være en av [application/pdf, image/jpeg, image/png]",
  "instance": "about:blank"
}
```

Må sendes som en multipart/form-data hvor parten har name=vedlegg og Content-Type header satt

#### Hente vedlegg

GET @/vedlegg/{uuid} (som er url'en returnert som 'Location' header ved opplasting -> 200 Response med vedlegget

##### Feilmeldinger

```json
{
  "type": "/problem-details/attachment-not-found",
  "title": "attachment-not-found",
  "status": 404,
  "detail": "Inget vedlegg funnet med etterspurt ID.",
  "instance": "about:blank"
}
```

#### Slette vedlegg

DELETE @/vedlegg/{uuid} (som er url'en returnert som 'Location' header ved opplasting -> 204 response

### Hente arbeidsgivere

- Query parameter 'fra_og_til' og 'til_og_med' må settes til datoer
  GET @ /arbeidsgiver?fra_og_med=2019-01-20&til_og_med=2019-01-30 -> 200 Response

```json
{
  "organisasjoner": [
    {
      "navn": "Telenor",
      "organisasjonsnummer": "973861778"
    },
    {
      "navn": "Maxbo",
      "organisasjonsnummer": "910831143"
    }
  ]
}
```

### Hente barn

GET @ /barn -> 200 Response

```json
{
  "barn": [
    {
      "fødselsdato": "2000-08-27",
      "fornavn": "BARN",
      "mellomnavn": "EN",
      "etternavn": "BARNESEN",
      "aktørId": "1000000000001"
    },
    {
      "fødselsdato": "2001-04-10",
      "fornavn": "BARN",
      "mellomnavn": "TO",
      "etternavn": "BARNESEN",
      "aktørId": "1000000000002"
    }
  ]
}
```

## Serialisering av datoer, tidspunkt og varighet

API'et returnerer på format ISO 8601

- Dato: 2018-12-18
- Tidspunkt: 2018-12-18T10:43:32Z
- Varighet: PT7H30M (7 timer og 30 minutter), PT30M (30 minutter) PT0S (0)

## Feilsituasjoner

API'et returnerer feilkoder (http > 300) etter [RFC7807](https://tools.ietf.org/html/rfc7807)

### HTTP 400

```json
{
  "type": "/problem-details/invalid-json-entity",
  "title": "invalid-json-entity",
  "status": 400,
  "detail": "Request entityen inneholder ugyldig JSON.",
  "instance": "about:blank"
}
```

```json
{
  "type": "/problem-details/invalid-request-parameters",
  "title": "invalid-request-parameters",
  "status": 400,
  "detail": "Requesten inneholder ugylidge parametre.",
  "instance": "about:blank",
  "invalid_parameters": [
    {
      "type": "query",
      "name": "fraOgMed",
      "reason": "Må settes og være på gyldig format (YYYY-MM-DD)",
      "invalid_value": null
    },
    {
"type":_"query",
      "name": "tilOgMed",
      "reason": "Må settes og være på og gyldig format (YYYY-MM-DD)",
      "invalid_value": null
    }
  ]
}
```

```json
{
    "type": "/problem-details/invalid-request-parameters",
    "title": "invalid-request-parameters",
    "status": 400,
    "detail": "Requesten inneholder ugyldige paramtere.",
    "instance": "about:blank",
    "invalid_parameters": [
    {
      "type": "entity",
      "name": "arbeidsgivere.organisasjoner[0].skalJobbeProsent && arbeidsgivere.organisasjoner[0].skalJobbe",
      "reason": "skalJobbeProsent er ikke 0%. Dersom skalJobbe = 'vet ikke', så må skalJobbeProsent være mellom 0%",
      "invalid_value": [
        {
          "navn": "Bjeffefirmaet ÆÆÅ",
          "skalJobbe": "vet_ikke",
          "organisasjonsnummer": "917755736",
          "jobberNormaltTimer": null,
          "skalJobbeProsent": 10.0,
          "vetIkkeEkstrainfo": null
        }
      ]
    }
  ]
}
```

```json
{
  "type": "/problem-details/invalid-request-parameters",
  "title": "invalid-request-parameters",
  "status": 400,
  "detail": "Requesten inneholder ugyldige paramtere.",
  "instance": "about:blank",
  "invalid_parameters": [
    {
      "type": "entity",
      "name": "arbeidsgivere.organisasjoner[0].skalJobbeProsent && arbeidsgivere.organisasjoner[0].skalJobbe",
      "reason": "skalJobbeProsent er ulik 0%. Dersom skalJobbe = 'nei', så må skalJobbeProsent være 0%",
      "invalid_value": [
        {
          "navn": "Bjeffefirmaet ÆÆÅ",
          "skalJobbe": "nei",
          "organisasjonsnummer": "917755736",
          "jobberNormaltTimer": null,
          "skalJobbeProsent": 10.0,
          "vetIkkeEkstrainfo": null
        }
      ]
    }
  ]
}
```

```json
{
  "type": "/problem-details/invalid-request-parameters",
  "title": "invalid-request-parameters",
  "status": 400,
  "detail": "Requesten inneholder ugyldige paramtere.",
  "instance": "about:blank",
  "invalid_parameters": [
    {
      "type": "entity",
      "name": "arbeidsgivere.organisasjoner[0].skalJobbeProsent && arbeidsgivere.organisasjoner[0].skalJobbe",
      "reason": "skalJobbeProsent ligger ikke mellom 1% og 99%. Dersom skalJobbe = 'redusert', så må skalJobbeProsent være mellom 1% og 99%",
      "invalid_value": [
        {
          "navn": "Bjeffefirmaet ÆÆÅ",
          "skalJobbe": "redusert",
          "organisasjonsnummer": "917755736",
          "jobberNormaltTimer": null,
          "skalJobbeProsent": 100.0,
          "vetIkkeEkstrainfo": null
        }
      ]
    }
  ]
}
```

````json
{
  "type": "/problem-details/invalid-request-parameters",
  "title": "invalid-request-parameters",
  "status": 400,
  "detail": "Requesten inneholder ugyldige paramtere.",
  "instance": "about:blank",
  "invalid_parameters": [
    {
      "type": "entity",
      "name": "arbeidsgivere.organisasjoner[0].skalJobbeProsent && arbeidsgivere.organisasjoner[0].skalJobbe",
      "reason": "skalJobbeProsent er ulik 100%. Dersom skalJobbe = 'ja', så må skalJobbeProsent være 100%",
      "invalid_value": [
        {
          "navn": "Bjeffefirmaet ÆÆÅ",
          "skalJobbe": "ja",
          "organisasjonsnummer": "917755736",
          "jobberNormaltTimer": null,
          "skalJobbeProsent": 99.0,
          "vetIkkeEkstrainfo": null
        }
      ]
    }
  ]
}
````

### HTTP 401

Ikke autentisert. Ingen payload i resposnen.

### HTTP 403

Ikke autorisert til å fullføre requesten. Ingen payload i responsen.

### HTTP 500

```json
{
  "type": "/problem-details/unhandled-error",
  "title": "unhandled-error",
  "status": 500,
  "detail": "En uhåndtert feil har oppstått.",
  "instance": "about:blank"
}
```

### Mellomlagre sesjon

POST @ /mellomlagring -> 200 Response
GET @ /mellomlagring -> 200 Response
DELETE @ /mellomlagring -> 200 Response

### Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerts.yml](nais/alerts.yml).

### Redis

Vi bruker Redis for mellomlagring. En instanse av Redis må være kjørene før deploy av applikasjonen.
Dette gjøres manuelt med kubectl både i preprod og prod. Se [nais/doc](https://github.com/nais/doc/blob/master/content/redis.md)

1. `kubectl config use-context preprod-sbs`
2. `kubectl apply -f redis-config.yml`

## Starte opp lokalt

Kjør klassen ApplicationWithMocks som er en del av testkoden.
Dette vil først starte en wiremock server som mocker ut alle eksterne http-kall.

### Logg inn

Gå på, eller legg inn følgende URL som URL til Login Service

`http://localhost:8081/login-service/v1.0/login?redirect={REDIRECT_URL}&fnr={FNR}`

fra SokerResponseTransformer.kt

`http://localhost:8081/login-service/v1.0/login?redirect=http://localhost:8080/&fnr=25037139184`

Dette vil sette en cookie som gjør at du er autentisert og kommer forbi 401/403-feil.

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #team-düsseldorf.
