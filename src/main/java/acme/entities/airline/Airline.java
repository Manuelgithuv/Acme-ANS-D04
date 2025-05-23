
package acme.entities.airline;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidEmail;
import acme.client.components.validation.ValidMoment;
import acme.client.components.validation.ValidString;
import acme.client.components.validation.ValidUrl;
import acme.constraints.ValidShortText;
import acme.datatypes.AirlineType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Airline extends AbstractEntity {

	private static final long	serialVersionUID	= 1L;

	@Mandatory
	@ValidShortText
	@Automapped
	private String				name;

	@Mandatory
	@ValidString(pattern = "^[A-Z]{3}$", message = "IATA code must be a 3-letter uppercase code")
	@Column(unique = true)
	private String				iataCode;

	@Mandatory
	@ValidUrl
	@Automapped
	private String				webSite;

	@Mandatory
	@Valid
	@Automapped
	private AirlineType			type;

	@Mandatory
	@ValidMoment(past = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date				foundationMoment;

	@Optional
	@ValidEmail
	@Automapped
	private String				email;

	@Mandatory
	@ValidString(pattern = "^\\+?\\d{6,15}$", message = "Phone number must match the pattern ^\\+?\\d{6,15}$")
	@Automapped
	private String				phoneNumber;

}
