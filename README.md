# pleiepengesoknad-api 

API for Selvbetjeningsløsning for søknad om pleiepenger

[![CircleCI](https://circleci.com/gh/navikt/pleiepengesoknad-api/tree/master.svg?style=svg)](https://circleci.com/gh/navikt/pleiepengesoknad-api/tree/master)

Benyttet av [pleiepengesoknad](https://github.com/navikt/pleiepengesoknad)

## Endepunkt
### Sende inn søknad
POST @ /soknad -> 202 Response
- Listen med arbeidsgivere inneholder data på samme format som GET @ /arbeidsgiver, med to valgfrie attributter (om en er satt må begge settes);
- arbeidsgivere.organisasjoner[x].normal_arbeidsuke settes til varigheten på en normal arbeidsuke for søkeren hos arbeidsgiveren. (Se format i eget avsnitt)
- arbeidsgivere.organisasjoner[x].redusert_arbeidsuke settes til varighet på arbeidsukene for søkeren hos arbeidsgiveren i perioden det søkes pleiepenger for. (Se format i eget avsnitt)
- Listen med organisajoner i arbeidsgivere kan være tom.
- Vedlegg er en liste med URL'er som peker tilbake på 'Location' headeren returnert i opplasting av vedlegg
- Det må sendes med minst ett vedlegg
- Det kan settes kun 1 ID på barnet (alternativ_id, aktoer_id eller fodselsnummer) - Kan også sendes uten ID
- barn.alternativ_id må være 11 siffer om det er satt
- barn.fodslsnummer må være et gyldig norsk fødselsnummer om den er satt
- barn.aktoer_id må være sattil en gyldig Aktør ID om den er satt
- barn.navn er kun påkrevd om 'barn.fodselsnummer' er satt
- relasjon_til_barnet er ikke påkrevd om 'barn.aktoer_id' er satt, ellers påkrevd
- grad må være satt til en verdi større eller lik 20 og mindre eller lik 100
- 'har_bekreftet_opplysninger' og 'har_forstatt_rettigheter_og_plikter' må være true

```json
{
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
		"organisasjoner": [{
			"navn": "Telenor",
			"organisasjonsnummer": "973861778",
			"normal_arbeidsuke": "PT7H30M",
			"redusert_arbeidsuke": "PT5H"
		}, {
			"navn": "Maxbo",
			"organisasjonsnummer": "910831143"
		}]
	},
	"vedlegg": [
		"http://pleiepengesoknad-api.nav.no/vedlegg/e2daa60b-2423-401c-aa33-b41dc6b630e7"
	],
	"medlemskap": {
		"har_bodd_i_utlandet_siste_12_mnd": false,
		"skal_bo_i_utlandet_neste_12_mnd": true
	},
	"har_medsoker": true,
	"har_bekreftet_opplysninger" : true,
	"har_forstatt_rettigheter_og_plikter": true,
	"grad": 100
}
```

#### Feilmeldinger
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
  "organisasjoner" : [ {
    "navn" : "Telenor",
    "organisasjonsnummer" : "973861778"
  }, {
    "navn" : "Maxbo",
    "organisasjonsnummer" : "910831143"
  } ]
}
```

### Hente barn
GET @ /barn -> 200 Response
```json
{
    "barn": [{
        "fodselsdato": "2000-08-27",
        "fornavn": "BARN",
        "mellomnavn": "EN",
        "etternavn": "BARNESEN",
        "aktoer_id": "1000000000001"
    }, {
        "fodselsdato": "2001-04-10",
        "fornavn": "BARN",
        "mellomnavn": "TO",
        "etternavn": "BARNESEN",
        "aktoer_id": "1000000000002"
    }]
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
	"invalid_parameters": [{
		"type": "query",
		"name": "fra_og_med",
		"reason": "Må settes og være på gyldig format (YYYY-MM-DD)",
		"invalid_value": null
	}, {
		"type": "query",
		"name": "til_og_med",
		"reason": "Må settes og være på og gyldig format (YYYY-MM-DD)",
		"invalid_value": null
	}]
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


## Starte opp lokalt
Kjør klassen ApplicationWithMocks som er en del av testkoden.
Dette vil først starte en wiremock server som mocker ut alle eksterne http-kall.

### Logg inn
Gå på, eller legg inn følgende URL som URL til Login Service 

`http://localhost:8081/login-service/v1.0/login?redirect={REDIRECT_URL}&fnr={FNR}`

Dette vil sette en cookie som gjør at du er autentisert og kommer forbi 401/403-feil.

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #team-düsseldorf.
