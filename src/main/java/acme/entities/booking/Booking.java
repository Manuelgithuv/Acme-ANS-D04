
package acme.entities.booking;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.datatypes.Money;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidMoment;
import acme.client.components.validation.ValidMoney;
import acme.client.components.validation.ValidString;
import acme.datatypes.TravelClass;
import acme.entities.flight.Flight;
import acme.realms.Customer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "booking", indexes = {
	@Index(name = "idx_booking_customer_id", columnList = "customer_id"), @Index(name = "idx_booking_published", columnList = "published"), @Index(name = "idx_booking_locator_code", columnList = "locatorCode")
})
public class Booking extends AbstractEntity {

	private static final long	serialVersionUID	= 1L;

	@Mandatory
	@ValidString(pattern = "^[A-Z0-9]{6,8}$")
	@Column(unique = true)
	private String				locatorCode;

	@Mandatory
	@ValidMoment(past = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date				purchaseMoment;

	@Mandatory
	@Valid
	@Automapped
	private TravelClass			travelClass;

	@Mandatory
	@ValidMoney
	@Automapped
	private Money				price;

	@Mandatory
	@Automapped
	private boolean				published;

	@Optional
	@ValidString(pattern = "^\\d{4}$")
	@Automapped
	private String				lastCardNibble;

	// Relaciones 
	// -------------------------------------------------------------------

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Customer			customer;

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Flight				flight;

}
