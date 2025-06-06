
package acme.features.flight_crew.activity_log;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activity_log.ActivityLog;
import acme.realms.FlightCrew;

@GuiService
public class CrewActivityLogDeleteService extends AbstractGuiService<FlightCrew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private ActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		int crewId = super.getRequest().getPrincipal().getActiveRealm().getId();
		int id;

		try {
			id = super.getRequest().hasData("id") ? super.getRequest().getData("id", int.class) : 0;

			ActivityLog log = this.repository.findById(id);
			status = log != null;
			if (!status) {
				super.getResponse().setAuthorised(status);
				return;
			}

			boolean isLogPublished = log != null && log.getPublished();
			boolean isLogFromAuthenticatedCrew = log != null && log.getFlightAssignment().getAssignee().getId() == crewId;
			status = !isLogPublished && isLogFromAuthenticatedCrew;
		} catch (Exception e) {
			status = false;
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		ActivityLog log;
		int id;

		id = super.getRequest().getData("id", int.class);
		log = this.repository.findById(id);

		super.getBuffer().addData(log);
	}

	@Override
	public void bind(final ActivityLog log) {
		super.bindObject(log);
	}

	@Override
	public void validate(final ActivityLog log) {
		;
	}

	@Override
	public void perform(final ActivityLog log) {
		this.repository.delete(log);
	}

	@Override
	public void unbind(final ActivityLog log) {
		Dataset dataset;
		dataset = super.unbindObject(log, "registrationMoment", "incidentType", "description", "severity");
		super.getResponse().addData(dataset);
	}

}
