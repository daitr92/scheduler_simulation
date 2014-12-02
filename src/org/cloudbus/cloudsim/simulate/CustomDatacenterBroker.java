package org.cloudbus.cloudsim.simulate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

public class CustomDatacenterBroker extends DatacenterBroker {
	public static final int STOPPED = 0;
	public static final int RUNNING = 1;
	
	
	private List<? extends ResCloudlet> estimationList;
	private int estimationStatus = STOPPED;

	public CustomDatacenterBroker(String name) throws Exception {
		super(name);
		setEstimationList(new ArrayList<ResCloudlet>());
	}
	
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;

			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}
	
	@Override
	protected void submitCloudlets() {
		Log.printLine(this.getName() + " submit Cloudlet");
		for (Cloudlet cloudlet: getCloudletList()) {
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			
			Log.printLine("Cloudlet #" + cloudlet.getCloudletId() + " has been submitted!");
		}
	}
	
	@Override
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
//		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}
	
	public void addDatacenter(int datacenterId) {
		getDatacenterIdsList().add(datacenterId);
	}


	public int getEstimationStatus() {
		return estimationStatus;
	}


	public void setEstimationStatus(int estimationStatus) {
		this.estimationStatus = estimationStatus;
	}


	public List<? extends ResCloudlet> getEstimationList() {
		return estimationList;
	}


	public void setEstimationList(List<? extends ResCloudlet> estimationList) {
		this.estimationList = estimationList;
	}

}
