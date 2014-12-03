package org.cloudbus.cloudsim.simulate;

import java.util.List;

import org.cloudbus.cloudsim.Log;

public class EstimationCloudletObserve {
	private List<Integer> datacenterList;
	private CustomResCloudlet resCloudlet;
	private int datacenterIdOFCurrrentExecVm;
	
	public EstimationCloudletObserve(CustomResCloudlet resCloudlet, List<Integer> datacenterList) {
		this.datacenterList = datacenterList;
		this.resCloudlet = resCloudlet;
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
		Log.printLine("DEBUG");
		Log.printLine(resCloudlet.getBestFinishTime());
		Log.printLine(resCloudlet.getCloudlet().getDeadlineTime());
		return resCloudlet.getBestFinishTime() <= resCloudlet.getCloudlet().getDeadlineTime();
	}

	public int getDatacenterIdOFCurrrentExecVm() {
		return datacenterIdOFCurrrentExecVm;
	}

	public void setDatacenterIdOFCurrrentExecVm(int datacenterIdOFCurrrentExecVm) {
		this.datacenterIdOFCurrrentExecVm = datacenterIdOFCurrrentExecVm;
	}
}
