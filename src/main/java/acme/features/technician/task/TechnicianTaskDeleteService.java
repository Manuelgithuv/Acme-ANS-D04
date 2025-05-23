
package acme.features.technician.task;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.datatypes.TaskType;
import acme.entities.involves.Involves;
import acme.entities.task.Task;
import acme.features.technician.involves.TechnicianInvolvesRepository;
import acme.realms.Technician;

@GuiService
public class TechnicianTaskDeleteService extends AbstractGuiService<Technician, Task> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private TechnicianTaskRepository		repository;

	@Autowired
	private TechnicianInvolvesRepository	repositoryInvolves;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {
		boolean exist;
		Task task;
		Technician technician;

		int id = super.getRequest().hasData("id") ? super.getRequest().getData("id", int.class) : 0;
		task = this.repository.findTaskById(id);

		exist = task != null;
		if (exist) {
			technician = (Technician) super.getRequest().getPrincipal().getActiveRealm();
			if (technician.equals(task.getTechnician()) && task.isDraftMode())
				super.getResponse().setAuthorised(true);
		}
	}

	@Override
	public void load() {
		Task task;
		int id;

		id = super.getRequest().getData("id", int.class);
		task = this.repository.findTaskById(id);

		super.getBuffer().addData(task);
	}

	@Override
	public void bind(final Task task) {
		super.bindObject(task, "type", "description", "priority", "estimatedDuration");
	}

	@Override
	public void validate(final Task task) {
		;
	}

	@Override
	public void perform(final Task task) {
		Collection<Involves> involvesAsociadas = this.repositoryInvolves.findAllInvolvesByTaskId(task.getId());

		this.repositoryInvolves.deleteAll(involvesAsociadas);

		this.repository.delete(task);
	}

	@Override
	public void unbind(final Task task) {
		SelectChoices choices;

		Dataset dataset;
		choices = SelectChoices.from(TaskType.class, task.getType());

		dataset = super.unbindObject(task, "type", "description", "priority", "estimatedDuration", "draftMode");
		dataset.put("type", choices.getSelected().getKey());
		dataset.put("type", choices);

		super.getResponse().addData(dataset);
	}

}
