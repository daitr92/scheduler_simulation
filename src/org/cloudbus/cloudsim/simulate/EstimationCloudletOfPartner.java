package org.cloudbus.cloudsim.simulate;

import java.util.List;

import org.cloudbus.cloudsim.Log;

public class EstimationCloudletOfPartner {
	
	private CustomResCloudlet resCloudlet;
	private List<PartnerInfomation> partnerIdsList;
	private List<PartnerInfomation> globalpartnerLists;
	private int currentBestPartnerId;
	private PartnerInfomation currentBestPartner;
	
	public EstimationCloudletOfPartner(CustomResCloudlet resCloudlet,
			List<PartnerInfomation> partnerIdsList,List<PartnerInfomation> globalpartnerLists) {
		super();
		this.resCloudlet = resCloudlet;
		this.partnerIdsList = partnerIdsList;
		this.setGlobalpartnerLists(globalpartnerLists);
		this.currentBestPartnerId = -1;
		this.currentBestPartner = new PartnerInfomation(-1);
		this.currentBestPartner.setkRatio(-1);
	}
	
	/**
	 * Get result & find the best partner base on ratio 
	 * Trying to find best k ratio, not best finish time
	 * @param partnerId
	 * @param reResCloudlet
	 * @return
	 */
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
		
		if (bestFinishTime < resCloudlet.getCloudlet().getDeadlineTime()){
			double k = calcPartnerKRatio(partnerId,reResCloudlet);
			Log.printLine("Cloulet :"+reResCloudlet.getCloudletId()+" K of partner#: "+partnerId + "; K: "+k);
			if((getCurrentBestPartner().getPartnerId()  == -1) || getCurrentBestPartner().getkRatio() < k || getCurrentBestPartner().getkRatio()  == -1){
				currentBestPartner.setPartnerId(partnerId);
				getCurrentBestPartner().setkRatio(k);
				currentBestPartner.setRequested(reResCloudlet.getCloudlet().getCloudletLength());
//				Log.printLine(getCurrentBestPartner().getkRatio());
				partner_cancel_waiting_exec[0] = currentBestPartnerId;
				partner_cancel_waiting_exec[1] = resCloudlet.getBestDatacenterId();
				partner_cancel_waiting_exec[2] = resCloudlet.getBestVmId();
				
				resCloudlet.setBestFinishTime(bestFinishTime);
				resCloudlet.setBestDatacenterId(reResCloudlet.getBestDatacenterId());
				resCloudlet.setBestVmId(reResCloudlet.getBestVmId());
				currentBestPartnerId = partnerId;
				
			} 
//			else if( getCurrentBestPartner().getkRatio() == k ) {
//				if(resCloudlet.getBestFinishTime() > reResCloudlet.getBestFinishTime()){
//					currentBestPartner.setPartnerId(partnerId);
//					getCurrentBestPartner().setkRatio(k);
//					currentBestPartner.setRequested(reResCloudlet.getCloudlet().getCloudletLength());
////					Log.printLine(getCurrentBestPartner().getkRatio());
//					partner_cancel_waiting_exec[0] = currentBestPartnerId;
//					partner_cancel_waiting_exec[1] = resCloudlet.getBestDatacenterId();
//					partner_cancel_waiting_exec[2] = resCloudlet.getBestVmId();
//					
//					resCloudlet.setBestFinishTime(bestFinishTime);
//					resCloudlet.setBestDatacenterId(reResCloudlet.getBestDatacenterId());
//					resCloudlet.setBestVmId(reResCloudlet.getBestVmId());
//					currentBestPartnerId = partnerId;
//				}
//			}
		}

		return partner_cancel_waiting_exec;
	}
	
	
//	public int[] receiveEstimateResult(int partnerId, CustomResCloudlet reResCloudlet) {
//		int[] partner_cancel_waiting_exec = new int[3];
//		partner_cancel_waiting_exec[0] = partnerId;
//		partner_cancel_waiting_exec[1] = reResCloudlet.getBestDatacenterId();
//		partner_cancel_waiting_exec[2] = reResCloudlet.getBestVmId();
//		
//		int totalPartnerId = partnerIdsList.size();
//		for (int i = 0; i < totalPartnerId; i++) {
//			if (partnerIdsList.get(i).getPartnerId() == partnerId) {
//				partnerIdsList.remove(i);
//				break;
//			}
//		}
//		
//		double bestFinishTime = reResCloudlet.getBestFinishTime();
//		
//		if (bestFinishTime < resCloudlet.getCloudlet().getDeadlineTime() && bestFinishTime < resCloudlet.getBestFinishTime()) {
//			partner_cancel_waiting_exec[0] = currentBestPartnerId;
//			partner_cancel_waiting_exec[1] = resCloudlet.getBestDatacenterId();
//			partner_cancel_waiting_exec[2] = resCloudlet.getBestVmId();
//			
//			resCloudlet.setBestFinishTime(bestFinishTime);
//			resCloudlet.setBestDatacenterId(reResCloudlet.getBestDatacenterId());
//			resCloudlet.setBestVmId(reResCloudlet.getBestVmId());
//
//			currentBestPartnerId = partnerId;
//		}
//
//		return partner_cancel_waiting_exec;
//	}
	
	
	
	public double calcPartnerKRatio(int partnerID,CustomResCloudlet resCloudlet) {
		double kRatio  = 1;
		for(PartnerInfomation pInfo :globalpartnerLists){
			if(pInfo.getPartnerId() == partnerID){
				kRatio = pInfo.getKRatioWithCurrentTask(resCloudlet.getCloudlet().getCloudletLength(), 0);
			}
		}
		return kRatio;
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
		boolean is_ontime = resCloudlet.getBestFinishTime() <= resCloudlet.getCloudlet().getDeadlineTime();
//		boolean is_satify_ratio  = getCurrentBestPartner().getkRatio() > 0;
		boolean is_satify_ratio  = true;
		return (is_ontime && is_satify_ratio);
	}

	public int getCurrentBestPartnerId() {
		return currentBestPartnerId;
	}

	/**
	 * @return the currentBestPartner
	 */
//	public PartnerInfomation getCurrentBestPartner() {
//		return currentBestPartner;
//	}

	/**
	 * @param currentBestPartner the currentBestPartner to set
	 */
	public void setCurrentBestPartnerId(int currentBestPartnerId) {
		this.currentBestPartnerId = currentBestPartnerId;
	}

	public List<PartnerInfomation> getGlobalpartnerLists() {
		return globalpartnerLists;
	}

	public void setGlobalpartnerLists(List<PartnerInfomation> globalpartnerLists) {
		this.globalpartnerLists = globalpartnerLists;
	}

	public PartnerInfomation getCurrentBestPartner() {
		return currentBestPartner;
	}

	public void setCurrentBestPartner(PartnerInfomation currentBestPartner) {
		this.currentBestPartner = currentBestPartner;
	}
}
