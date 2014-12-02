package org.cloudbus.cloudsim.simulate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public class CustomDatacenterBroker extends DatacenterBroker {
	public static final int STOPPED = 0;
	public static final int RUNNING = 1;
	
	
	
	private List<Cloudlet> estimationList;
	private int estimationStatus = STOPPED;
	
	protected Map<Integer, Map<Integer, EstimationCloudletObserve>> estimateCloudletMap;
	private List<PartnerInfomation> partnersList = new ArrayList<PartnerInfomation>();  

	public CustomDatacenterBroker(String name) throws Exception {
		super(name);
		setEstimationList(new ArrayList<Cloudlet>());
		setPartnersList(new ArrayList<PartnerInfomation>());
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
			case CloudSimTags.BROKER_ESTIMATE_NEXT_TASK:
				estimateNextTask();
				break;
			case CloudSimTags.BROKER_ESTIMATE_REQUEST:
				processPartnerCloudletEstimateRequest(ev);
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
			addCloudletToEstimationList(cloudlet);
			
			Log.printLine("Cloudlet #" + cloudlet.getCloudletId() + " has been submitted!");
		}
	}
	private void addCloudletToEstimationList(Cloudlet cloudlet) {
		getEstimationList().add(cloudlet);
		if (estimationStatus == STOPPED) {
			setEstimationStatus(RUNNING);
			sendNow(getId(), CloudSimTags.BROKER_ESTIMATE_NEXT_TASK);
		}
	}
	
	private void estimateNextTask() {
		if (getEstimationList().isEmpty()) {
			setEstimationStatus(STOPPED);
		} else {
			Cloudlet cloudlet = getEstimationList().get(0);
			for (Integer datacenterId: getDatacenterIdsList()) {
				CustomResCloudlet rcl = new CustomResCloudlet(cloudlet);
				sendNow(datacenterId, CloudSimTags.DATACENTER_ESTIMATE_TASK, rcl);
			}
		}
	}
	
	@Override
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	
		buildPartnerInfoList(CloudSim.getEntityList());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}
	//TODO; implement it, i just copy it, 
	@Override	
	protected void processPartnerCloudletEstimateRequest(SimEvent ev){
		Map<Integer, Map<Integer, EstimationCloudletObserve>> estimateCloudletMap = getEstimateCloudletMap();
		if (!estimateCloudletMap.containsKey(ev.getSource())) {
			Map<Integer, EstimationCloudletObserve> cloudletList = new HashMap<Integer, EstimationCloudletObserve>();
			estimateCloudletMap.put(ev.getSource(), cloudletList);
		}
		Map<Integer, EstimationCloudletObserve> cloudletList = estimateCloudletMap.get(ev.getSource());
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		
		List<Integer> datacenterIDs = new LinkedList<Integer>();
		for (Integer integer : getDatacenterIdsList()) {
			datacenterIDs.add(integer);
		}
		ResCloudlet resCloudlet = new ResCloudlet(cloudlet);
		resCloudlet.setFinishTime(Double.MAX_VALUE);
		
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Received partner estimate cloudlet #"+resCloudlet.getCloudletId());
		EstimationCloudletObserve eco = new EstimationCloudletObserve(resCloudlet, datacenterIDs);
		
		cloudletList.put(new Integer(cloudlet.getCloudletId()), eco);
		if(datacenterIDs.size() == 0 ){
			Log.printLine(getName()+ " has no datacenter, can not estimate");
		}
		for (int i: datacenterIDs) {
			Object[] data = {ev.getSource(), cloudlet};
			sendNow(i, CloudSimTags.PARTNER_INTERNAL_ESTIMATE_REQUEST, data);
		}
		
//		Object[] timeoutData = {ev.getSource(), cloudlet.getCloudletId()};
//		send(getId(), 30, CloudSimTags.PARTNER_ESTIMATE_TIMEOUT, timeoutData);
	}
	
	public void addDatacenter(int datacenterId) {
		getDatacenterIdsList().add(datacenterId);
	}

	/**
	 * Create list of partner information
	 * @param List o add Entity on system.
	 */
	private void buildPartnerInfoList(List<SimEntity> entityList) {
		for(SimEntity en: entityList){
			if (en instanceof DatacenterBroker  && en.getId() != getId()) {
				//TODO: i'm hardcode the ratio by 1. fix it;
			 PartnerInfomation partnerInfoItem   = new PartnerInfomation(en.getId(), 1);
			 this.getPartnersList().add(partnerInfoItem);
			}
		}
		Log.printLine("Debug: partner info list of borker: "+ getName());
		for( PartnerInfomation pt:  this.getPartnersList()){
			Log.printLine(pt.toString());
		}
		Log.printLine("");
	}
	
	/**
	 * getter and setter area
	 * @return
	 */

	public int getEstimationStatus() {
		return estimationStatus;
	}


	public void setEstimationStatus(int estimationStatus) {
		this.estimationStatus = estimationStatus;
	}


	public List<Cloudlet> getEstimationList() {
		return estimationList;
	}


	public void setEstimationList(List<Cloudlet> estimationList) {
		this.estimationList = estimationList;
	}

	public Map<Integer, Map<Integer, EstimationCloudletObserve>> getEstimateCloudletMap() {
		return estimateCloudletMap;
	}

	public void setEstimateCloudletMap(
			Map<Integer, Map<Integer, EstimationCloudletObserve>> estimateCloudletMap) {
		this.estimateCloudletMap = estimateCloudletMap;
	}

	public List<PartnerInfomation> getPartnersList() {
		return partnersList;
	}

	public void setPartnersList(List<PartnerInfomation> partnersList) {
		this.partnersList = partnersList;
	}

}
