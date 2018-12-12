package no.nav.pleiepenger.api.general.validation

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Validator

class ValidationHandler(
    private val validator: Validator,
    private val objectMapper: ObjectMapper
) {
    fun <T> validate(input: T) {

        val logger: Logger = LoggerFactory.getLogger("validationStatusPages")


        val constraints = validator.validate(input)

        if (constraints.isNotEmpty()) {
            val violations = mutableListOf<Violation>()

            for (it in constraints) {
                // TODO: Seems like this should be possible to do better...
                var invalidValue = if (it.invalidValue != null) objectMapper.writeValueAsString(it.invalidValue) else null
                if (invalidValue != null) invalidValue = invalidValue.removePrefix("\"").removeSuffix("\"")

                logger.info("Invalid Value = '{}'", invalidValue)
                violations.add(
                    Violation(
                        name = it.propertyPath.toString(),
                        reason = it.message,
                        invalidValue = invalidValue
                    )
                )
            }

            throw ValidationException(violations)
        }
    }
}