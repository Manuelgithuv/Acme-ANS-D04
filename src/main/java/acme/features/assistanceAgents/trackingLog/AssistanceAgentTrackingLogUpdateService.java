
package acme.features.assistanceAgents.trackingLog;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.claim.Claim;
import acme.entities.claimLog.ClaimTrackingLog;
import acme.features.assistanceAgents.claim.AssistanceAgentClaimRepository;
import acme.realms.AssistanceAgent;

@GuiService
public class AssistanceAgentTrackingLogUpdateService extends AbstractGuiService<AssistanceAgent, ClaimTrackingLog> {

	@Autowired
	private AssistanceAgentTrackingLogRepository	repository;

	@Autowired
	private AssistanceAgentClaimRepository			claimRepository;


	@Override
	public void authorise() {

		boolean status;

		int agentId = super.getRequest().getPrincipal().getActiveRealm().getId();

		int id = super.getRequest().hasData("id") ? super.getRequest().getData("id", int.class) : 0;

		ClaimTrackingLog claimLog = this.repository.findClaimTrackingLogById(id);
		boolean claimCheck = true;

		if (!super.getRequest().getMethod().equals("GET")) {

			int claimId = super.getRequest().getData("claim", int.class);
			Claim claim = this.claimRepository.findClaimById(claimId);
			if (claim != null && claim.getAssistanceAgent().getId() == agentId)
				claimCheck = true;
			else if (claimId == 0)
				claimCheck = true;
			else
				claimCheck = false;
		}
		status = claimLog.getClaim().getAssistanceAgent().getId() == agentId && !claimLog.isPublished() && claimCheck;

		super.getResponse().setAuthorised(status);

	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		ClaimTrackingLog claimLog = this.repository.findClaimTrackingLogById(id);
		super.getBuffer().addData(claimLog);
	}

	@Override
	public void bind(final ClaimTrackingLog claimLog) {
		int claimId = super.getRequest().getData("claim", int.class);
		Claim claim = this.claimRepository.findClaimById(claimId);

		String money = super.getRequest().getData("compensation", String.class);
		if (money == "") {
			claimLog.setCompensation(null);
			super.bindObject(claimLog, "lastUpdateMoment", "stepUndergoing", "resolutionPercentage", "resolutionDescription");
		} else
			super.bindObject(claimLog, "lastUpdateMoment", "stepUndergoing", "resolutionPercentage", "resolutionDescription", "compensation");
		claimLog.setClaim(claim);
	}

	@Override
	public void validate(final ClaimTrackingLog claimLog) {

		if (claimLog.getClaim() == null)
			super.state(false, "claim", "assistance-agent.claim-tracking-log.create.claim");

	}

	@Override
	public void perform(final ClaimTrackingLog claimLog) {
		this.repository.save(claimLog);
	}

	@Override
	public void unbind(final ClaimTrackingLog claimLog) {
		int agentId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Dataset dataset = super.unbindObject(claimLog, "lastUpdateMoment", "stepUndergoing", "resolutionPercentage", "resolutionDescription", "published", "compensation");
		if (claimLog.getClaim() != null)
			dataset.put("claimAcepted", claimLog.getIsAcepted());
		else
			dataset.put("claimAcepted", false);
		Collection<Claim> claims = this.claimRepository.findAllByAssistanceAgentId(agentId);
		SelectChoices claimChoices = SelectChoices.from(claims, "id", claimLog.getClaim());
		dataset.put("claim", claimChoices.getSelected().getKey());
		dataset.put("claims", claimChoices);

		if (claimLog.isPublished())
			dataset.put("readonly", true);
		super.getResponse().addData(dataset);
	}
}
