package org.cloudbus.cloudsim.simulate;

import java.util.List;

public class EstimationCloudletOfPartner {
	
	private CustomResCloudlet resCloudlet;
	private List<PartnerInfomation> partnerIdsList;
	private int currentBestPartnerId;
	
	public EstimationCloudletOfPartner(CustomResCloudlet resCloudlet,
			List<PartnerInfomation> partnerIdsList) {
		super();
		this.resCloudlet = resCloudlet;
		this.partnerIdsList = partnerIdsList;
		this.currentBestPartnerId = -1;
	}
	
	public int[] receiveEstimateResult(int partnerId, CustomResCloudlet reResCloudlet) {
		int[] partner_cancel_waiting_exec = new int[3];
		partner_cancel_waiting_exec[0] = partnerId;
		partner_cancel_waiting_exec[1] = reResCloudlet.getBestDatacenterId();
		partner_cancel_waiting_exec[2] = reResCloudlet.getBestVmId();
		
		int totalPartnerId = partnerIdsList.size();
		for (int i = 0; i < totalPartnerId; i++) {
			if (partnerIdsList.get(i).getPartnerId() == partnerId) {
				partnerIdsList.remove(i);
				break;
			}
		}
		
		double bestFinishTime = reResCloudlet.getBestFinishTime();
		
		if (bestFinishTime < resCloudlet.getCloudlet().getDeadlineTime() && bestFinishTime < resCloudlet.getBestFinishTime()) {
			partner_cancel_waiting_exec[0] = currentBestPartnerId;
			partner_cancel_waiting_exec[1] = resCloudlet.getBestDatacenterId();
			partner_cancel_waiting_exec[2] = resCloudlet.getBestVmId();
			
			resCloudlet.setBestFinishTime(bestFinishTime);
			resCloudlet.setBestDatacenterId(reResCloudlet.getBestDatacenterId());
			resCloudlet.setBestVmId(reResCloudlet.getBestVmId());

			currentBestPartnerId = partnerId;
		}

		return partner_cancel_waiting_exec;
	}
	
	public CustomResCloudlet getResCloudlet() {
		return resCloudlet;
	}

	public void setResCloudlet(CustomResCloudlet resCloudlet) {
		this.resCloudlet = resCloudlet;
	}

	public List<PartnerInfomation> getPartnerIdsList() {
		return partnerIdsList;
	}

	public void setPartnerIdsList(List<PartnerInfomation> partnerIdsList) {
		this.partnerIdsList = partnerIdsList;
	}
	
	public boolean isFinished() {
		return partnerIdsList.size() == 0;
	}
	
	public boolean isExecable() {
		return resCloudlet.getBestFinishTime() <= resCloudlet.getCloudlet().getDeadlineTime();
	}

	public int getCurrentBestPartnerId() {
		return currentBestPartnerId;
	}

	public void setCurrentBestPartnerId(int currentBestPartnerId) {
		this.currentBestPartnerId = currentBestPartnerId;
	}
}
