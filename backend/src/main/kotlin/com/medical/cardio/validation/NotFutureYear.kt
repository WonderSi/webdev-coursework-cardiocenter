package com.medical.cardio.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.Year
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotFutureYearValidator::class])
annotation class NotFutureYear(
    val message: String = "Год не может быть больше текущего",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NotFutureYearValidator : ConstraintValidator<NotFutureYear, Int> {
    override fun isValid(value: Int?, context: ConstraintValidatorContext) =
        value == null || value <= Year.now().value
}
