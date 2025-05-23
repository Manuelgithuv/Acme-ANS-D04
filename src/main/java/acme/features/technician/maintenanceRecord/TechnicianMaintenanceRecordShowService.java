
package acme.features.technician.maintenanceRecord;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.datatypes.AircraftStatus;
import acme.datatypes.MaintenanceRecordStatus;
import acme.entities.aircraft.Aircraft;
import acme.entities.maintenanceRecord.MaintenanceRecord;
import acme.realms.Technician;

@GuiService
public class TechnicianMaintenanceRecordShowService extends AbstractGuiService<Technician, MaintenanceRecord> {

	//Internal state ----------------------------------------------------------

	@Autowired
	private TechnicianMaintenanceRecordRepository repository;

	//AbstractGuiService state ----------------------------------------------------------


	@Override
	public void authorise() {
		MaintenanceRecord maintenanceRecord;
		int id;

		id = super.getRequest().hasData("id") ? super.getRequest().getData("id", int.class) : 0;
		maintenanceRecord = this.repository.findMaintenanceRecordById(id);
		Technician technician = (Technician) super.getRequest().getPrincipal().getActiveRealm();

		if (id != 0)
			if (technician.equals(maintenanceRecord.getTechnician()))
				super.getResponse().setAuthorised(true);

	}

	@Override
	public void load() {
		MaintenanceRecord maintenanceRecord;
		int id;

		id = super.getRequest().getData("id", int.class);
		maintenanceRecord = this.repository.findMaintenanceRecordById(id);

		super.getBuffer().addData(maintenanceRecord);
	}

	@Override
	public void unbind(final MaintenanceRecord maintenanceRecord) {

		SelectChoices choices;
		SelectChoices aircraft;
		Collection<Aircraft> aircrafts;
		MaintenanceRecord maintenanceRecord1;
		int id = super.getRequest().getData("id", int.class);

		maintenanceRecord1 = this.repository.findMaintenanceRecordById(id);

		Dataset dataset;
		aircrafts = this.repository.findAllAircrafts().stream().filter(a -> a.getStatus().equals(AircraftStatus.UNDER_MAINTENANCE) || maintenanceRecord1.getAircraft().getId() == a.getId()).toList();
		choices = SelectChoices.from(MaintenanceRecordStatus.class, maintenanceRecord.getStatus());
		aircraft = SelectChoices.from(aircrafts, "id", maintenanceRecord.getAircraft());

		dataset = super.unbindObject(maintenanceRecord, "moment", "status", "inspectionDueDate", "estimatedCost", "notes", "aircraft", "draftMode");
		dataset.put("status", choices);
		dataset.put("aircraft", aircraft);

		super.getResponse().addData(dataset);
	}

}
