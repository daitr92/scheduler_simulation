package org.cloudbus.cloudsim.simulate;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.ResCloudlet;

public class CustomResCloudlet extends ResCloudlet {	
	private int bestVmId;
	private double bestFinishTime = Double.MAX_VALUE;
	private int bestDatacenterId;
	
	public CustomResCloudlet(Cloudlet cloudlet) {
		super(cloudlet);
	}

	public double getBestFinishTime() {
		return bestFinishTime;
	}

	public void setBestFinishTime(double bestFinishTime) {
		this.bestFinishTime = bestFinishTime;
	}

	public int getBestVmId() {
		return bestVmId;
	}

	public void setBestVmId(int bestVmId) {
		this.bestVmId = bestVmId;
	}

	public int getBestDatacenterId() {
		return bestDatacenterId;
	}

	public void setBestDatacenterId(int bestDatacenterId) {
		this.bestDatacenterId = bestDatacenterId;
	}
}
