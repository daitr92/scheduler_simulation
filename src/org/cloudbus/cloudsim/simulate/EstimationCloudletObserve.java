package org.cloudbus.cloudsim.simulate;

import java.util.List;

<<<<<<< HEAD
public class EstimationCloudletObserve {
	private List<Integer> datacenterList;
	private CustomResCloudlet resCloudlet;
=======
import org.cloudbus.cloudsim.ResCloudlet;

public class EstimationCloudletObserve {
	private List<Integer> datacenterList;
	private CustomResCloudlet resCloudlet;
	private int datacenterIdOFCurrrentExecVm;
>>>>>>> origin/master
	
	public EstimationCloudletObserve(CustomResCloudlet resCloudlet, List<Integer> datacenterList) {
		this.datacenterList = datacenterList;
		this.resCloudlet = resCloudlet;
<<<<<<< HEAD
=======
		this.setDatacenterIdOFCurrrentExecVm(-1);
>>>>>>> origin/master
	}
	
	public List<Integer> getDatacenterList() {
		return datacenterList;
	}
	
	public void setDatacenterList(List<Integer> datacenterList) {
		this.datacenterList = datacenterList;
	}
	
<<<<<<< HEAD
	public CustomResCloudlet getResCloudlet() {
=======
	public ResCloudlet getResCloudlet() {
>>>>>>> origin/master
		return resCloudlet;
	}
	
	public void setResCloudlet(CustomResCloudlet resCloudlet) {
		this.resCloudlet = resCloudlet;
	}
<<<<<<< HEAD
	
	public void receiveEstimateResult(int datacenterID, CustomResCloudlet reResCloudlet) {
=======
	//if resturn datacenterID that will be cancel exec;
	public int receiveEstimateResult(int datacenterID, CustomResCloudlet reResCloudlet,Boolean result) {
		int DatacenterCancelExec = datacenterID;
>>>>>>> origin/master
		int totalDatacenter = datacenterList.size();
		for (int i = 0; i < totalDatacenter; i++) {
			if (datacenterList.get(i) == datacenterID) {
				datacenterList.remove(i);
				break;
			}
		}
<<<<<<< HEAD
		
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
=======
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
>>>>>>> origin/master
	}
}
