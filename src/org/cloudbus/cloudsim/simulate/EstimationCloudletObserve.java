package org.cloudbus.cloudsim.simulate;

import java.util.List;

import org.cloudbus.cloudsim.ResCloudlet;

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
	
	public ResCloudlet getResCloudlet() {
		return resCloudlet;
	}
	
	public void setResCloudlet(CustomResCloudlet resCloudlet) {
		this.resCloudlet = resCloudlet;
	}
	//if resturn datacenterID that will be cancel exec;
	public int receiveEstimateResult(int datacenterID, CustomResCloudlet reResCloudlet,Boolean result) {
		int DatacenterCancelExec = datacenterID;
		int totalDatacenter = datacenterList.size();
		for (int i = 0; i < totalDatacenter; i++) {
			if (datacenterList.get(i) == datacenterID) {
				datacenterList.remove(i);
				break;
			}
		}
//		if(reResCloudlet.getCloudlet().getDeadline() >= reResCloudlet.getClouddletFinishTime()  ){
			double finishTime = reResCloudlet.getClouddletFinishTime();
			double bestFinishTime = resCloudlet.getClouddletFinishTime();
//			Log.printLine(finishTime);
//			Log.printLine(bestFinishTime);
//			Log.printLine("-----------------------------------------------------------------");
//			Log.printLine(resCloudlet.getClouddletFinishTime());
//			Log.printLine(DatacenterCancelExec);
			if (finishTime > 0 && finishTime < bestFinishTime) {
				DatacenterCancelExec = getDatacenterIdOFCurrrentExecVm();
				setNewVmToExce(datacenterID,finishTime,reResCloudlet);
			}
//		}
		return DatacenterCancelExec;
	}
	
	private void setNewVmToExce(int datacenterID,double finishTime, ResCloudlet reResCloudlet) {
		setDatacenterIdOFCurrrentExecVm(datacenterID);
		resCloudlet.setFinishTime(finishTime);
		resCloudlet.getCloudlet().setVmId(reResCloudlet.getCloudlet().getVmId()); 
	}

	public boolean isFinished() {
		return datacenterList.size() == 0;
	}

	public int getDatacenterIdOFCurrrentExecVm() {
		return datacenterIdOFCurrrentExecVm;
	}

	public void setDatacenterIdOFCurrrentExecVm(int datacenterIdOFCurrrentExecVm) {
		this.datacenterIdOFCurrrentExecVm = datacenterIdOFCurrrentExecVm;
	}
}
