package org.cloudbus.cloudsim.simulate;

import java.util.List;

public class EstimationCloudletObserve {
	private List<Integer> datacenterList;
	private CustomResCloudlet resCloudlet;
	private int datacenterIdOFCurrrentExecVm;
	
	public EstimationCloudletObserve(CustomResCloudlet resCloudlet, List<Integer> datacenterList) {
		this.datacenterList = datacenterList;
		this.resCloudlet = resCloudlet;
		this.resCloudlet.setBestDatacenterId(-1);
		this.setDatacenterIdOFCurrrentExecVm(-1);
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
	
	//if resturn datacenterID that will be cancel exec;
	public int[] receiveEstimateResult(int datacenterID, CustomResCloudlet reResCloudlet) {
		int[] cancel_waiting_exec = new int[2];
		cancel_waiting_exec[0] = datacenterID;
		cancel_waiting_exec[1] = reResCloudlet.getBestVmId();
		
		int totalDatacenter = datacenterList.size();
		for (int i = 0; i < totalDatacenter; i++) {
			if (datacenterList.get(i) == datacenterID) {
				datacenterList.remove(i);
				break;
			}
		}
		
		double bestFinishTime = reResCloudlet.getBestFinishTime();
		
		if (bestFinishTime < resCloudlet.getCloudlet().getDeadlineTime() && bestFinishTime < resCloudlet.getBestFinishTime()) {
			cancel_waiting_exec[0] = resCloudlet.getBestDatacenterId();
			cancel_waiting_exec[1] = resCloudlet.getBestVmId();
			
			resCloudlet.setBestFinishTime(bestFinishTime);
			resCloudlet.setBestDatacenterId(datacenterID);
			resCloudlet.setBestVmId(reResCloudlet.getBestVmId());
		}
		
		return cancel_waiting_exec;
	}
	
	public boolean isFinished() {
		return datacenterList.size() == 0;
	}
	
	public boolean isExecable() {
		return resCloudlet.getBestFinishTime() <= resCloudlet.getCloudlet().getDeadlineTime();
	}

	public int getDatacenterIdOFCurrrentExecVm() {
		return datacenterIdOFCurrrentExecVm;
	}

	public void setDatacenterIdOFCurrrentExecVm(int datacenterIdOFCurrrentExecVm) {
		this.datacenterIdOFCurrrentExecVm = datacenterIdOFCurrrentExecVm;
	}
}
