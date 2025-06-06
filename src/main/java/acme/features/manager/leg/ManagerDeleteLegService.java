
package acme.features.manager.leg;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.datatypes.LegStatus;
import acme.entities.aircraft.Aircraft;
import acme.entities.aircraft.AircraftRepository;
import acme.entities.airport.Airport;
import acme.entities.airport.AirportRepository;
import acme.entities.leg.Leg;
import acme.realms.Manager;

@GuiService
public class ManagerDeleteLegService extends AbstractGuiService<Manager, Leg> {

	@Autowired
	private LegRepository		legRepository;

	@Autowired
	private AirportRepository	airportRepository;

	@Autowired
	private AircraftRepository	aircraftRepository;


	@Override
	public void authorise() {
		boolean status;

		int managerId = super.getRequest().getPrincipal().getActiveRealm().getId();

		int id = super.getRequest().hasData("id")?super.getRequest().getData("id", int.class):0;

		Leg leg = this.legRepository.findById(id);
		
		int aircraftId = super.getRequest().hasData("aircraft")? super.getRequest().getData("aircraft",int.class):0;
		
		int departureId = super.getRequest().hasData("departureAirport")? super.getRequest().getData("departureAirport",int.class):0;
		
		int arrivalId = super.getRequest().hasData("arrivalAirport")? super.getRequest().getData("arrivalAirport",int.class):0;
		
		boolean entitiesExist = true;
		
		if (aircraftId != 0 && this.aircraftRepository.findById(aircraftId) == null) {
	        entitiesExist = false;
	        
	    }
	

		if (departureId != 0 && this.airportRepository.findById(departureId) == null) {
	        entitiesExist = false;
	        
	    }
	

		if (arrivalId != 0 && this.airportRepository.findById(arrivalId) == null) {
	        entitiesExist = false;
	        
	    }

		status = leg != null && leg.getManager().getId() == managerId  && !leg.isPublished() && entitiesExist;

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {

		Leg leg;

		int id;

		id = super.getRequest().getData("id", int.class);

		leg = this.legRepository.findById(id);

		super.getBuffer().addData(leg);

	}
	@Override
	public void bind(final Leg leg) {

		int aircraftId;
		Aircraft aircraft;

		int departureAirportId;
		Airport departureAirport;

		int arrivalAirportId;
		Airport arrivalAirport;

		aircraftId = super.getRequest().getData("aircraft", int.class);
		aircraft = this.aircraftRepository.findById(aircraftId);

		departureAirportId = super.getRequest().getData("departureAirport", int.class);
		departureAirport = this.airportRepository.findById(departureAirportId);

		arrivalAirportId = super.getRequest().getData("arrivalAirport", int.class);
		arrivalAirport = this.airportRepository.findById(arrivalAirportId);

		super.bindObject(leg, "flightCode", "scheduledDeparture", "scheduledArrival", "status", "hours");

		leg.setAircraft(aircraft);
		leg.setDepartureAirport(departureAirport);
		leg.setArrivalAirport(arrivalAirport);

	}

	@Override
	public void validate(final Leg leg) {
		;
	}

	@Override
	public void perform(final Leg leg) {
		this.legRepository.delete(leg);

	}

	@Override
	public void unbind(final Leg leg) {
		Dataset dataset = this.buildDataset(leg);

		this.populateDatasetWithChoices(dataset, leg);

		super.getResponse().addData(dataset);
	}

	private Dataset buildDataset(final Leg leg) {
		return super.unbindObject(leg, "flightCode", "scheduledDeparture", "scheduledArrival", "status", "hours", "published");
	}

	private void populateDatasetWithChoices(final Dataset dataset, final Leg leg) {
		Collection<Airport> airports = this.airportRepository.findAllAirports();
		Collection<Aircraft> aircrafts = this.aircraftRepository.findAllActiveAircrafts();

		SelectChoices departureAirportChoices = SelectChoices.from(airports, "iataCode", leg.getDepartureAirport());
		SelectChoices arrivalAirportChoices = SelectChoices.from(airports, "iataCode", leg.getArrivalAirport());
		Aircraft aircraft = leg.getAircraft() == null || leg.getAircraft().getId() == 0|| !aircrafts.contains(leg.getAircraft()) ? null : leg.getAircraft();
		SelectChoices aircraftChoices = SelectChoices.from(aircrafts, "registrationNumber", aircraft);
		SelectChoices statusChoices = SelectChoices.from(LegStatus.class, leg.getStatus());

		dataset.put("departureAirport", departureAirportChoices.getSelected().getKey());
		dataset.put("departureAirports", departureAirportChoices);
		dataset.put("arrivalAirport", arrivalAirportChoices.getSelected().getKey());
		dataset.put("arrivalAirports", arrivalAirportChoices);
		dataset.put("aircraft", aircraftChoices.getSelected().getKey());
		dataset.put("aircrafts", aircraftChoices);
		dataset.put("statuses", statusChoices);

	}

}
