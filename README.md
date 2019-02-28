# API for Selvbetjeningsløsning for søknad om pleiepenger
Benyttet av [pleiepengesoknad](https://github.com/navikt/pleiepengesoknad)

## Endepunkt
### Sende inn søknad
POST @ /soknad -> 202 Response
- Listen med arbeidsgivere inneholder data på samme format som GET @ /arbeidsgiver
- Vedlegg er en liste med URL'er som peker tilbake på 'Location' headeren returnert i opplasting av vedlegg
- Det må sendes med minst ett vedlegg
- Ingen detaljer om barnet er påkrevd
- barn.alternativ_id må være 11 siffer om det er satt
- barn.fodslsnummer må være et gyldig norsk fødselsnummer om det er satt

```json
{
	"barn": {
		"navn": "Iben Olafsson Hansen",
		"fodselsnummer": "01011950021",
		"alternativ_id": null
	},
	"relasjon_til_barnet": "mor",
	"fra_og_med": "2019-10-10",
	"til_og_med": "2019-11-10",
	"arbeidsgivere": {
		"organisasjoner": [{
			"navn": "Telenor",
			"organisasjonsnummer": "973861778"
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
	}
}
```
### Søker
GET @/soker -> 200 Response
```json
{
    "etternavn": "MORSEN",
    "fornavn": "MOR",
    "mellomnavn": "HEISANN",
    "fodselsnummer": "290990123456"
}
```

### Vedlegg

#### Laste opp
POST @/vedlegg -> 201 Response med 'Location' header satt til vedlegget
##### Feilmeldinger
```json
{
  "type" : "/errors/multipart-form-required",
  "title" : "Requesten må være en 'multipart/form-data' request hvor en 'part' er en fil, har 'name=vedlegg' og har Content-Type header satt.",
  "status" : 400,
  "detail" : null,
  "instance" : "about:blank"
}
```

```json
{
  "type" : "/errors/attachment-not-attached",
  "title" : "Fant ingen 'part' som er en fil, har 'name=vedlegg' og har Content-Type header satt.",
  "status" : 400,
  "detail" : null,
  "instance" : "about:blank"
}
```

```json
{
  "type" : "/errors/attachment-too-large",
  "title" : "Vedlegget var over maks tillatt størrelse på 8MB.",
  "status" : 413,
  "detail" : null,
  "instance" : "about:blank"
}
```

Må sendes som en multipart/form-data hvor parten har name=vedlegg og Content-Type header satt

#### Hente vedlegg
GET @/vedlegg/{uuid} (som er url'en returnert som 'Location' header ved opplasting -> 200 Response med vedlegget
##### Feilmeldinger
```json
{
  "type" : "/errors/attachment-not-found",
  "title" : "Inget vedlegg funnet med etterspurt ID.",
  "status" : 404,
  "detail" : "Vedlegg med ID 123 ikke funnet.",
  "instance" : "about:blank"
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
  "barn" : [ {
    "fodselsdato" : "1990-09-29",
    "fornavn" : "Santa",
    "mellomnavn" : "Claus",
    "etternavn" : "Winter"
  }, {
    "fodselsdato" : "1966-06-06",
    "fornavn" : "George",
    "mellomnavn" : null,
    "etternavn" : "Costanza"
  } ]
}
```

## Serialisering av datoer og tidspunkt
API'et returnerer på format ISO 8601
- Dato: 2018-12-18
- Tidspunkt: 2018-12-18T10:43:32Z


## Feilsituasjoner
API'et returnerer feilkoder (http > 300) etter [RFC7807](https://tools.ietf.org/html/rfc7807)
### HTTP 400

```json
{
    "type": "/error/invalid-json",
    "title": "The provided entity is not a valid JSON object.",
    "status": 400,
    "detail": null,
    "instance": "about:blank"
}
```

### HTTP 401
```json
{
  "type" : "/errors/login-expired",
  "title" : "The login has expired.",
  "status" : 401,
  "detail" : "The Token has expired on Tue Dec 18 13:13:39 CET 2018.",
  "instance" : "about:blank"
}
```
```json
{
  "type" : "/errors/login-required",
  "title" : "Not logged in",
  "status" : 401,
  "detail" : "No cookie with name 'localhost-idtoken' set on request",
  "instance" : "about:blank"
}
```

```json
{
  "type" : "/errors/invalid-login",
  "title" : "The Claim 'iss' value doesn't match the required one.",
  "status" : 401,
  "detail" : null,
  "instance" : "about:blank"
}
```

### HTTP 403
```json
{
  "type" : "/errors/insufficient-authentication-level",
  "title" : "Insufficient authentication level to perform request.",
  "status" : 401,
  "detail" : "Requires authentication Level4, was 'Level2'",
  "instance" : "about:blank"
}
```

### HTTP 422
```json
{
    "type": "/errors/invalid-parameters",
    "title": "The provided JSON object contains invalid formatted parameters.",
    "status": 422,
    "detail": null,
    "instance": "about:blank",
    "invalid_parameters": [
        {
            "name": "dato",
            "reason": "must be a past date",
            "invalid_value": "2018-12-18T20:43:32Z"
        }
    ]
}
```

### HTTP 500
```json
{
  "type" : "about:blank",
  "title" : "Unhandled error",
  "status" : 500,
  "detail" : null,
  "instance" : "about:blank"
}
```


## Starte opp lokalt
Kjør klassen ApplicationWithMocks som er en del av testkoden.
Dette vil først starte en wiremock server som mocker ut alle eksterne http-kall.

### Logg inn
Gå på `http://localhost:8081/auth-mock/cookie?subject={fodselsnummer}&redirect_location={url_to_client}`
Dette vil sette en cookie som gjør at du er autentisert og kommer forbi 401/403-feil.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #område-helse.
