package no.nav.helse.general.validation

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.prometheus.client.Counter
import no.nav.helse.general.error.DefaultError
import no.nav.helse.general.error.monitorException
import java.net.URI

fun StatusPages.Configuration.validationStatusPages(
    errorCounter: Counter
) {

    val invalidParametersType = URI.create("/errors/invalid-parameters")
    val invalidParametersTitle = "The provided JSON object contains invalid formatted parameters."
    val invalidJsonType = URI.create("/error/invalid-json")


    /**
     * Missing not nullable fields in kotlin data classes
     */
    exception<MissingKotlinParameterException> { cause ->
        monitorException(cause, invalidParametersType, errorCounter)
        val errors: MutableList<Violation> = mutableListOf()
        cause.path.forEach {
            if (it.fieldName != null) {
                errors.add(
                    Violation(
                        name = it.fieldName,
                        reason = "can not be null"
                    ))
            }
        }
        call.respond(
            HttpStatusCode.UnprocessableEntity, ValidationError(
                status = HttpStatusCode.UnprocessableEntity.value,
                type = invalidParametersType,
                title =  invalidParametersTitle,
                invalidParameters = errors
            )
        )
        throw cause
    }


    /**
     * Properly formatted JSON object, but contains entities on an invalid format
     */
    exception<InvalidFormatException> { cause ->
        monitorException(cause, invalidParametersType, errorCounter)

        val fieldName: String = cause.path.first().fieldName

        call.respond(
            HttpStatusCode.UnprocessableEntity, ValidationError(
                status = HttpStatusCode.UnprocessableEntity.value,
                type = invalidParametersType,
                title = invalidParametersTitle,
                invalidParameters = listOf(
                    Violation(
                        name = fieldName,
                        reason = cause.message,
                        invalidValue = if (cause.value != null) cause.value.toString() else null
                    )
                )
            )
        )

        throw cause
    }

    /**
     * Errors validating objects from their annotations
     */
    exception<ValidationException> { cause ->
        monitorException(cause, invalidParametersType, errorCounter)

        call.respond(
            HttpStatusCode.UnprocessableEntity, ValidationError(
                status = HttpStatusCode.UnprocessableEntity.value,
                type = invalidParametersType,
                title = invalidParametersTitle,
                invalidParameters = cause.violations
            )
        )

        throw cause
    }

    /**
     * Invalid formatted JSON object
     */
    exception<JsonProcessingException> { cause ->
        monitorException(cause, invalidJsonType, errorCounter)

        call.respond(
            HttpStatusCode.BadRequest, DefaultError(
                status = HttpStatusCode.BadRequest.value,
                type = invalidJsonType,
                title = "The provided entity is not a valid JSON object."
            )
        )

        throw cause
    }
}