# API for Selvbetjeningsløsning for søknad om pleiepenger
Benyttet av [pleiepengesoknad](https://github.com/navikt/pleiepengesoknad)

## Endepunkt
### Sende inn søknad
POST @ /soknad -> 202 Response
```json

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
