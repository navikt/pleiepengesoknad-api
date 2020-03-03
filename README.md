# pleiepengesoknad-api

API for Selvbetjeningsløsning for søknad om pleiepenger

[![CircleCI](https://circleci.com/gh/navikt/pleiepengesoknad-api/tree/master.svg?style=svg)](https://circleci.com/gh/navikt/pleiepengesoknad-api/tree/master)

Benyttet av [pleiepengesoknad](https://github.com/navikt/pleiepengesoknad)

## Endepunkt

### Sende inn søknad

POST @ /soknad -> 202 Response

- sprak er en valgfri attributt. Om den settes må den være enten "nb" for Bokmål eller "nn" for Nynorsk.
- Listen med arbeidsgivere inneholder data på samme format som GET @ /arbeidsgiver, med to valgfrie attributter (om en er satt må begge settes);
- arbeidsgivere.organisasjoner[x].skal_jobbe_prosent er valgfri. Om satt må den være mellom 0.0 og 100.0. Om 'grad' ikke er satt må denne alltid settes.
- Listen med organisajoner i arbeidsgivere kan være tom.
- Vedlegg er en liste med URL'er som peker tilbake på 'Location' headeren returnert i opplasting av vedlegg
- Det må sendes med minst ett vedlegg
- Det kan settes kun 1 ID på barnet (alternativ_id, aktoer_id eller fodselsnummer) - Kan også sendes uten ID
- barn.alternativ_id må være 11 siffer om det er satt
- barn.fodslsnummer må være et gyldig norsk fødselsnummer om den er satt
- barn.aktoer_id må være sattil en gyldig Aktør ID om den er satt
- barn.navn er kun påkrevd om 'barn.fodselsnummer' er satt
- relasjon_til_barnet er ikke påkrevd om 'barn.aktoer_id' er satt, ellers påkrevd
- grad er valgfri. Om satt må den være mellom 20 og 100
- 'har_bekreftet_opplysninger' og 'har_forstatt_rettigheter_og_plikter' må være true
- 'dager_per_uke_borte_fra_jobb' er valgfri. Om satt må den være mellom 0.5 og 5.0. Om 'grad' ikke er satt og 'har_medsoker' er true må den settes.
- tilsynsordning er ikke påkrevd, se eget avsnitt under om det er satt.
- beredskap er ikke påkrevd, men om det er satt må 'i_beredskap' være satt og 'tilleggsinformasjon' være maks 1000 tegn om den er satt.
- nattevaak er ikke påkrevd, men om det er satt må 'har_nattevaak' være satt og 'tilleggsinformasjon' være maks 1000 tegn om den er satt.

```json
{
  "sprak": "nb",
  "barn": {
    "navn": "Iben Olafsson Hansen",
    "fodselsnummer": "01011950021",
    "alternativ_id": null,
    "aktoer_id": null
  },
  "relasjon_til_barnet": "mor",
  "fra_og_med": "2019-10-10",
  "til_og_med": "2019-11-10",
  "arbeidsgivere": {
    "organisasjoner": [
      {
        "navn": "Telenor",
        "organisasjonsnummer": "973861778",
        "skal_jobbe": "ja",
        "skal_jobber_prosent": 50.24
      },
      {
        "navn": "Maxbo",
        "organisasjonsnummer": "910831143",
        "skal_jobbe": "ja",
        "skal_jobbe_prosent": 25.0
      }
    ]
  },
  "vedlegg": [
    "http://pleiepengesoknad-api.nav.no/vedlegg/e2daa60b-2423-401c-aa33-b41dc6b630e7"
  ],
  "medlemskap": {
    "har_bodd_i_utlandet_siste_12_mnd": false,
    "skal_bo_i_utlandet_neste_12_mnd": true,
    "utenlandsopphold_neste_12_mnd": [
      {
        "fra_og_med": "2019-10-10",
        "til_og_med": "2019-11-10",
        "landkode": "SE",
        "landnavn": "Sverige"
      }
    ],
    "utenlandsopphold_siste_12_mnd": []
  },
  "utenlandsopphold_i_perioden": {
    "skal_oppholde_seg_i_i_utlandet_i_perioden": true,
    "opphold": [
      {
        "fra_og_med": "2019-10-10",
        "til_og_med": "2019-11-10",
        "landkode": "SE",
        "landnavn": "Sverige"
      },
      {
        "landnavn": "USA",
        "landkode": "US",
        "fra_og_med": "2020-01-08",
        "til_og_med": "2020-01-09",
        "er_utenfor_eos": true,
        "er_barnet_innlagt": true,
        "arsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
      }
    ]
  },
  "ferieuttak_i_perioden": {
    "skal_ta_ut_ferie_i_periode": true,
    "ferieuttak": [
      {
        "fra_og_med": "2020-01-05",
        "til_og_med": "2020-01-07"
      }
    ]
  },
  "har_medsoker": true,
  "har_bekreftet_opplysninger": true,
  "har_forstatt_rettigheter_og_plikter": true,
  "grad": 100,
  "dager_per_uke_borte_fra_jobb": 4.5,
  "tilsynsordning": {
    "svar": "ja",
    "ja": {
      "mandag": "PT7H30M",
      "tirsdag": null,
      "onsdag": "PT7H25M",
      "torsdag": null,
      "fredag": "PT0S",
      "tilleggsinformasjon": "Unntatt uke 37. Da er han hjemme hele tiden."
    }
  },
  "beredskap": {
    "i_beredskap": true,
    "tilleggsinformasjon": "Må sitte utenfor barnehagen."
  },
  "nattevaak": {
    "har_nattevaak": true,
    "tilleggsinformasjon": "Må sove om dagen."
  },
  "har_hatt_inntekt_som_frilanser": true,
  "frilans": {
    "startdato": "2019-01-08",
    "jobber_fortsatt_som_frilans": true
  }
}
```

#### Utenlandsopphold i perioden

- Attributten `skal_oppholde_seg_i_utlandet_i_perioden` være satt. `true|false`.
- Attributten `opphold` inneholder to typer data

###### Opphold i EØS land

```json
{
  "landnavn": "Sverige",
  "landkode": "SE",
  "fra_og_med": "2020-01-01",
  "til_og_med": "2020-02-01"
}
```

###### Opphold i et land utenfor EØS

```json
{
  "landnavn": "USA",
  "landkode": "US",
  "fra_og_med": "2020-01-08",
  "til_og_med": "2020-01-09",
  "er_utenfor_eos": true,
  "er_barnet_innlagt": true,
  "arsak": "BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING"
}
```

- Attributten `er_utenfor_eos` er satt til `true`
- Attributten `er_barnet_innlagt` er satt til `true|false`
- Attributten `arsak` settes når `er_barnet_innlagt` er `true`. Gyldige verdier er:
  -- `BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING`
  -- `BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD`
  -- `ANNET`
- Attributten `arsak` settes til `null` når `er_barnet_innlagt` er `false`

#### Tilsynsordning

##### Ja

- Attributten `ja` være satt.
- Attributten `vet_ikke` kan ikke være satt.
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

- Hverken attributten `ja` eller `vet_ikke` kan være satt.

```json
{
  "svar": "nei"
}
```

##### Vet ikke

- Attributten `vet_ikke` være satt.
- Attributten `ja` kan ikke være satt.
- `vet_ikke.svar` kan være enten `er_sporadisk`, `er_ikke_laget_en_plan` eller `annet`
- Om `vet_ikke.svar` er `annet` må også attributten `vet_ikke.svar.annet` være satt, men være maks 140 tegn.

```json
{
  "svar": "vet_ikke",
  "vet_ikke": {
    "svar": "annet",
    "annet": "Blir for vanskelig å si nå."
  }
}
```

### Frilanser

### Del av søknadsobjekt

```typescript
{
    har_hatt_inntekt_som_frilanser?: boolean;
    frilans?: FrilansApiData;
}
```

#### Interface for api-data

```typescript
export interface FrilansApiData {
  startdato: ApiStringDate;
  jobber_fortsatt_som_frilans: boolean;
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
  "ANNET" = "ANNEN"
}

export interface VirksomhetApiData {
  naringstype: Næringstype[];
  fiskerErPåPlanB?: boolean;
  fra_og_med: ApiStringDate;
  til_og_med?: ApiStringDate | null;
  er_pagaende?: boolean;
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
  "fodselsnummer": "290990123456",
  "fodseldato": "1997-10-05",
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
      "fodselsdato": "2000-08-27",
      "fornavn": "BARN",
      "mellomnavn": "EN",
      "etternavn": "BARNESEN",
      "aktoer_id": "1000000000001"
    },
    {
      "fodselsdato": "2001-04-10",
      "fornavn": "BARN",
      "mellomnavn": "TO",
      "etternavn": "BARNESEN",
      "aktoer_id": "1000000000002"
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
      "name": "fra_og_med",
      "reason": "Må settes og være på gyldig format (YYYY-MM-DD)",
      "invalid_value": null
    },
    {
      "type": "query",
      "name": "til_og_med",
      "reason": "Må settes og være på og gyldig format (YYYY-MM-DD)",
      "invalid_value": null
    }
  ]
}
```

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
