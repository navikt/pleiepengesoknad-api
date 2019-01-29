# API for Selvbetjeningsløsning for søknad om pleiepenger
Benyttet av [pleiepengesoknad](https://github.com/navikt/pleiepengesoknad)

## Endepunkt
### Sende inn søknad
POST @ /soknad -> 202 Response
- mellomnavn må ikke settes
- Om fødselsdato kan avledes fra fødselsnummer trenger kun denne å bli sendt
- Listen med ansettelsesforhold inneholder data på samme format som GET @ /ansettelsesforhold
- Vedlegg som byte array eller base64. Må Være PDF, PNG eller JPG på mindre enn 8MB. Må sendes med minst ett vedlegg.

```json
{
	"barn": {
		"fornavn": "Iben",
		"mellomnavn": "Olafsson",
		"etternavn": "Hansen",
		"relasjon": "mor",
		"fodselsnummer": "01011950021",
		"fodselsdato": "2019-01-01"
	},
	"fra_og_med": "2019-10-10",
	"til_og_med": "2019-11-10",
	"ansettelsesforhold": {
		"organisasjoner": [{
			"navn": "Telenor",
			"organisasjonsnummer": "973861778"
		}, {
			"navn": "Maxbo",
			"organisasjonsnummer": "910831143"
		}]
	},
	"vedlegg": [{
		"innhold": [-1, -40, -1, -37, 0, -124, 0, 8, 6, 6, 7, 6, 5, 8, 7]
	}, {
		"innhold": "JVBERi0xLjQKJfbk/N8KMSAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovVmVyc2l"
	}]
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

### Hente ansettelsesforhold
GET @ /ansettelsesforhold -> 200 Response
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
