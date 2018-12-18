# API for Selvbetjeningsløsning for søknad om pleiepenger
Benyttet av [pleiepengesoknad](https://github.com/navikt/pleiepengesoknad)

## Endepunkt
### Hente barn
GET @ /barn
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

## Feilsituasjoner
API'et returnerer feilkoder (http > 300) etter [RFC7807](https://tools.ietf.org/html/rfc7807)

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
