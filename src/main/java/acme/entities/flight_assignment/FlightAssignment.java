
package acme.entities.flight_assignment;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidMoment;
import acme.constraints.ValidLongText;
import acme.datatypes.AssignmentStatus;
import acme.datatypes.CrewDuty;
import acme.entities.leg.Leg;
import acme.realms.FlightCrew;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "flight_assignment", indexes = {
	@Index(columnList = "assignee_id"), //
	@Index(columnList = "leg_id"), //
	@Index(columnList = "assignee_id, leg_id"), //
	@Index(columnList = "published"), //
	@Index(columnList = "leg_id, duty")
})
public class FlightAssignment extends AbstractEntity {

	/*
	 * A flight assignment represents the allocation of a flight crew member to a specific leg of a flight.
	 * Each assignment specifies the flight crew's duty in that leg ("PILOT", "CO-PILOT", "LEAD ATTENDANT",
	 * "CABIN ATTENDANT"), the moment of the last up-date (in the past), the current status of the assignment
	 * ("CONFIRMED", "PENDING", or "CANCELLED"), and some remarks (up to 255 characters), if necessary.
	 */

	// Serialisation version --------------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@Mandatory
	@Valid
	@Automapped
	private CrewDuty			duty;

	@Mandatory
	@ValidMoment(past = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date				lastUpdate;

	@Mandatory
	@Valid
	@Automapped
	private AssignmentStatus	status;

	@Optional
	@ValidLongText
	@Automapped
	private String				remarks;

	@Mandatory
	@Automapped
	private Boolean				published;

	// Derived attributes -----------------------------------------------------

	// Relationships ----------------------------------------------------------

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private FlightCrew			assignee;

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Leg					leg;

	// Methods ----------------------------------------------------------------


	public boolean existsConflict(final FlightAssignment newAssignment) {
		if (newAssignment.equals(this))
			return false;

		boolean conflicts;

		// fechas del assignment actual
		Date departure = this.getLeg().getScheduledDeparture();
		Date arrival = this.getLeg().getScheduledArrival();

		// fechas del nuevo assignment
		Date newDeparture = newAssignment.getLeg().getScheduledDeparture();
		Date newArrival = newAssignment.getLeg().getScheduledArrival();

		// comprobamos que la fecha de inicio del nuevo assignment no cae durante el actual
		conflicts = departure.before(newDeparture) && arrival.after(newDeparture);

		// comprobamos que la fecha de aterrizaje del nuevo assignment no cae durante el actual
		if (!conflicts)
			conflicts = departure.before(newArrival) && arrival.after(newArrival);

		return conflicts;
	}

}
