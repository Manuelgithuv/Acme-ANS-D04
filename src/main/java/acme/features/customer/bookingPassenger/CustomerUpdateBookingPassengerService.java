
package acme.features.customer.bookingPassenger;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingPassenger;
import acme.entities.passenger.Passenger;
import acme.features.customer.booking.BookingRepository;
import acme.features.customer.passenger.PassengerRepository;
import acme.realms.Customer;

@GuiService
public class CustomerUpdateBookingPassengerService extends AbstractGuiService<Customer, BookingPassenger> {

	@Autowired
	private BpRepository		bookingPassengerRepository;

	@Autowired
	private BookingRepository	bookingRepository;

	@Autowired
	private PassengerRepository	passengerRepository;


	@Override
	public void authorise() {

		boolean status;

		BookingPassenger bookingPassenger;

		int id = super.getRequest().hasData("id") ? super.getRequest().getData("id", int.class) : 0;

		bookingPassenger = this.bookingPassengerRepository.findById(id);

		Customer customer;

		customer = (Customer) super.getRequest().getPrincipal().getActiveRealm();

		boolean entitiesExist = true;

		if (!super.getRequest().getMethod().equals("GET")) {

			int bookingId = super.getRequest().getData("booking", int.class);
			int passengerId = super.getRequest().getData("passenger", int.class);

			if (bookingId != 0 && this.bookingRepository.findById(bookingId) == null)
				entitiesExist = false;

			if (passengerId != 0 && this.passengerRepository.findById(passengerId) == null)
				entitiesExist = false;
		}

		status = bookingPassenger != null && !bookingPassenger.isPublished() && customer.getId() == bookingPassenger.getCustomer().getId() && entitiesExist;

		super.getResponse().setAuthorised(status);

	}

	@Override
	public void load() {

		BookingPassenger bookingPassenger;

		int id;

		id = super.getRequest().getData("id", int.class);

		bookingPassenger = this.bookingPassengerRepository.findById(id);

		super.getBuffer().addData(bookingPassenger);

	}

	@Override
	public void bind(final BookingPassenger bookingPassenger) {
		int bookingId;
		Booking booking;

		int passengerId;
		Passenger passenger;

		bookingId = super.getRequest().getData("booking", int.class);
		booking = this.bookingRepository.findById(bookingId);

		passengerId = super.getRequest().getData("passenger", int.class);
		passenger = this.passengerRepository.findById(passengerId);

		super.bindObject(bookingPassenger);

		bookingPassenger.setBooking(booking);

		bookingPassenger.setPassenger(passenger);

	}

	@Override
	public void validate(final BookingPassenger bookingPassenger) {
		if (bookingPassenger.getBooking() == null) {
			super.state(false, "booking", "customer.bookingPassenger.create.null-booking");
			return;
		}

		if (bookingPassenger.getPassenger() == null) {
			super.state(false, "passenger", "customer.booking.create.null-passenger");
			return;
		}
		Optional<BookingPassenger> existingBookingPassenger = this.bookingPassengerRepository.findBookingPassengerByPassengerAndBooking(bookingPassenger.getPassenger().getId(), bookingPassenger.getBooking().getId());
		if (!existingBookingPassenger.isEmpty() && existingBookingPassenger.get().getId() != bookingPassenger.getId())
			super.state(false, "*", "customer.bookingPassenger.alreadyExists");
	}

	@Override
	public void perform(final BookingPassenger bookingPassenger) {

		this.bookingRepository.save(bookingPassenger);

	}

	@Override
	public void unbind(final BookingPassenger bookingPassenger) {

		Dataset dataset;
		Customer customer;
		customer = (Customer) super.getRequest().getPrincipal().getActiveRealm();
		Collection<Booking> bookings = this.bookingRepository.findBookingsNotPublishedByCustomerId(customer.getId());
		Booking booking = bookingPassenger.getBooking() == null || bookingPassenger.getBooking().getId() == 0 ? null : bookingPassenger.getBooking();

		SelectChoices bookingChoices = SelectChoices.from(bookings, "locatorCode", booking);

		Collection<Passenger> passengers = this.passengerRepository.findAvailablePassengers(customer.getId());
		Passenger passenger = bookingPassenger.getPassenger() == null || bookingPassenger.getPassenger().getId() == 0 ? null : bookingPassenger.getPassenger();

		SelectChoices passengerChoices = SelectChoices.from(passengers, "fullName", passenger);

		dataset = super.unbindObject(bookingPassenger, "published");

		dataset.put("booking", bookingChoices.getSelected().getKey());
		dataset.put("bookings", bookingChoices);
		dataset.put("passenger", passengerChoices.getSelected().getKey());
		dataset.put("passengers", passengerChoices);
		super.getResponse().addData(dataset);
	}

}
