package org.cloudbus.cloudsim.simulate;

import java.util.List;

public class EstimationCloudletOfPartner {
	
	private CustomResCloudlet resCloudlet;
	private List<Integer> partnerIdsList;
	private List<PartnerInfomation> partnerInfosList;
	private PartnerInfomation currentBestPartner;
	
	public EstimationCloudletOfPartner(CustomResCloudlet resCloudlet,
			List<Integer> partnerIdsList, List<PartnerInfomation> partnerInfosList) {
		super();
		this.resCloudlet = resCloudlet;
		this.partnerIdsList = partnerIdsList;
		this.setCurrentBestPartner(new PartnerInfomation(-1,-1,0,0,0,1));
		this.partnerInfosList = partnerInfosList;
	}
	
	public void receiveEstimateResult(int partnerId, CustomResCloudlet reResCloudlet) {
		int totalPartnerId = partnerIdsList.size();
		for (int i = 0; i < totalPartnerId; i++) {
			if (partnerIdsList.get(i) == partnerId) {
				partnerIdsList.remove(i);
				break;
			}
		}
		double finishTime = reResCloudlet.getClouddletFinishTime();
		double bestFinishTime = resCloudlet.getClouddletFinishTime();
		if ((bestFinishTime == -1 ||(finishTime > 0 && finishTime < bestFinishTime))){
			if(finishTime < reResCloudlet.getCloudlet().getDeadlineTime()){
				double k = calcPartnerKRatio(partnerId,reResCloudlet);
				if((getCurrentBestPartner().getPartnerId()  == -1) || getCurrentBestPartner().getkRatio() >k){
					getCurrentBestPartner().setPartnerId(partnerId);
					getCurrentBestPartner().setkRatio(k);
					getCurrentBestPartner().setRequested(reResCloudlet.getCloudlet().getCloudletLength());;
				}
				resCloudlet.setFinishTime(finishTime);
				resCloudlet.getCloudlet().setVmId(reResCloudlet.getCloudlet().getVmId());
			}
		}
	}
	
	public CustomResCloudlet getResCloudlet() {
		return resCloudlet;
	}

	public void setResCloudlet(CustomResCloudlet resCloudlet) {
		this.resCloudlet = resCloudlet;
	}

	
	public boolean isFinished() {
		if(partnerIdsList.size() == 0){
			for(PartnerInfomation pInfo :partnerInfosList){
				if(pInfo.getPartnerId() == getCurrentBestPartner().getPartnerId()){
					pInfo.updateLenghtRatio(getCurrentBestPartner().getRequested(), 0);
					pInfo.setRequested(getCurrentBestPartner().getRequested()+pInfo.getRequested());
				}
			}
		}
		return partnerIdsList.size() == 0;
	}
	
	public double calcPartnerKRatio(int partnerID,CustomResCloudlet resCloudlet) {
		double kRatio  = 1;
		for(PartnerInfomation pInfo :partnerInfosList){
			if(pInfo.getPartnerId() == partnerID){
				kRatio = pInfo.getKRatioWithCurrentTask(resCloudlet.getCloudlet().getCloudletLength(), 0);
			}
		}
		return kRatio;
	}

	public List<PartnerInfomation> getPartnerInfosList() {
		return partnerInfosList;
	}

	public void setPartnerInfosList(List<PartnerInfomation> partnerInfosList) {
		this.partnerInfosList = partnerInfosList;
	}
	
	public List<Integer> getPartnerIdsList() {
		return partnerIdsList;
	}

	public void setPartnerIdsList(List<Integer> partnerIdsList) {
		this.partnerIdsList = partnerIdsList;
	}

	/**
	 * @return the currentBestPartner
	 */
	public PartnerInfomation getCurrentBestPartner() {
		return currentBestPartner;
	}

	/**
	 * @param currentBestPartner the currentBestPartner to set
	 */
	public void setCurrentBestPartner(PartnerInfomation currentBestPartner) {
		this.currentBestPartner = currentBestPartner;
	}
	
	
	
}
