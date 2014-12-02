package org.cloudbus.cloudsim.simulate;

import java.util.List;

public class EstimationCloudletObserve {
	private List<Integer> datacenterList;
	private CustomResCloudlet resCloudlet;
	
	public EstimationCloudletObserve(CustomResCloudlet resCloudlet, List<Integer> datacenterList) {
		this.datacenterList = datacenterList;
		this.resCloudlet = resCloudlet;
	}
	
	public List<Integer> getDatacenterList() {
		return datacenterList;
	}
	
	public void setDatacenterList(List<Integer> datacenterList) {
		this.datacenterList = datacenterList;
	}
	
	public CustomResCloudlet getResCloudlet() {
		return resCloudlet;
	}
	
	public void setResCloudlet(CustomResCloudlet resCloudlet) {
		this.resCloudlet = resCloudlet;
	}
	
	public void receiveEstimateResult(int datacenterID, CustomResCloudlet reResCloudlet) {
		int totalDatacenter = datacenterList.size();
		for (int i = 0; i < totalDatacenter; i++) {
			if (datacenterList.get(i) == datacenterID) {
				datacenterList.remove(i);
				break;
			}
		}
		
		double bestFinishTime = reResCloudlet.getBestFinishTime();
		
		if (bestFinishTime < resCloudlet.getCloudlet().getDeadlineTime() && bestFinishTime < resCloudlet.getBestFinishTime()) {
			resCloudlet.setBestFinishTime(bestFinishTime);
			resCloudlet.setBestDatacenterId(datacenterID);
			resCloudlet.setBestVmId(reResCloudlet.getBestVmId());
		}
	}
	
	public boolean isFinished() {
		return datacenterList.size() == 0;
	}
	
	public boolean isExecable() {
		return resCloudlet.getBestFinishTime() <= resCloudlet.getCloudlet().getDeadlineTime();
	}
}
