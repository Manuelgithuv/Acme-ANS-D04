
package acme.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

import org.hibernate.validator.constraints.Range;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@ReportAsSingleViolation

@Range(min = 0, max = 500000)

public @interface ValidEarnedPoints {

	// Standard validation properties -----------------------------------------

	String message() default "{acme.validation.text.message}";

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

}
