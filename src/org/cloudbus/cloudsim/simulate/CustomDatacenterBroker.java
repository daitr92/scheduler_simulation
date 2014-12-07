package org.cloudbus.cloudsim.simulate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public class CustomDatacenterBroker extends DatacenterBroker {
	public static final int STOPPED = 0;
	public static final int RUNNING = 1;

	private Map<Integer, Map<Integer, EstimationCloudletObserve>> cloudletEstimateObserveMap;
	
	// list cloudlet waiting for internal estimate
	private List<Cloudlet> estimationList;
		
	private int estimationStatus = STOPPED;
	private List<PartnerInfomation> partnersList = new ArrayList<PartnerInfomation>();
	protected Map<Integer,EstimationCloudletOfPartner> estimateCloudletofParnerMap;
	
	public CustomDatacenterBroker(String name) throws Exception {
		super(name);
		setEstimationList(new ArrayList<Cloudlet>());
		setCloudletEstimateObserveMap(new HashMap<Integer, Map<Integer, EstimationCloudletObserve>>());
		setPartnersList(new ArrayList<PartnerInfomation>());
		setEstimateCloudletofParnerMap(new HashMap<Integer, EstimationCloudletOfPartner>());
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
				
			case CloudSimTags.BROKER_ESTIMATE_RETURN:
				processInternalEstimateReturn(ev);
				break;
				
			/* handle request send task to partner estimate form my datacenter  **/
			case CloudSimTags.PARTNER_INTERNAL_ESTIMATE_REQUEST:
				processPartnerCloudletInternalEstimateRequest(ev);
				break;
			/* handle request estimate from partner **/
			case CloudSimTags.PARTNER_ESTIMATE_REQUEST:
				handlerPartnerCloudletEstimateRequest(ev);
				break;
			//if the cloudle estimate result returned from partner
			case CloudSimTags.PARTNER_ESTIMATE_RETURN: 
				processReturnEstimateFromPartner(ev);
				break;
				
			case CloudSimTags.PARTNER_EXEC:
				processPartnerCloudletExecRequest(ev);
				break;
				
			case CloudSimTags.PARTNER_CANCEL_ESTIMATED_TASK:
				processPartnerCloudletCancelRequest(ev);
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
		Collections.sort(getEstimationList(), new CloudletComparator());
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
			createCloudletObserve(cloudlet);
			
			for (Integer datacenterId: getDatacenterIdsList()) {
				CustomResCloudlet rcl = new CustomResCloudlet(cloudlet);
//				send(datacenterId, CloudSimTags.DATACENTER_ESTIMATE_TASK, rcl);
				send(datacenterId, cloudlet.getUserRequestTime() - CloudSim.clock(), 
						CloudSimTags.DATACENTER_ESTIMATE_TASK, rcl);
			}
		}
	}
	
	private void createCloudletObserve(Cloudlet cloudlet) {
		int owner = cloudlet.getUserId();
		
		Map<Integer, EstimationCloudletObserve> observeMap;
		
		if (getCloudletEstimateObserveMap().containsKey(owner)) {
			observeMap = getCloudletEstimateObserveMap().get(owner);
		} else {
			observeMap = new HashMap<Integer, EstimationCloudletObserve>(); 
			getCloudletEstimateObserveMap().put(owner, observeMap);
		}
		
		EstimationCloudletObserve observe;
		if (observeMap.containsKey(cloudlet.getCloudletId())) {
			observe = observeMap.get(cloudlet.getCloudletId());
		} else {
			observe = new EstimationCloudletObserve(new CustomResCloudlet(cloudlet), new ArrayList<>(getDatacenterIdsList()));
			observeMap.put(cloudlet.getCloudletId(), observe);
		}
	}
	
	protected void processInternalEstimateReturn(SimEvent ev) {
		CustomResCloudlet re_rcl = (CustomResCloudlet) ev.getData();
		Log.printLine(getName() + ": Receive internal response from datacenter #" + ev.getSource() + ": " + re_rcl.getBestFinishTime());
		
		if (getCloudletEstimateObserveMap().containsKey(re_rcl.getUserId())) {
			Map<Integer, EstimationCloudletObserve> obserMap = getCloudletEstimateObserveMap().get(re_rcl.getUserId());
			EstimationCloudletObserve observe = obserMap.get(re_rcl.getCloudletId());
			int[] cancel_estimate = observe.receiveEstimateResult(ev.getSource(), re_rcl);
			
			if (cancel_estimate[0] > -1) {
				sendCancelRequest(cancel_estimate);
			}
			
			if (observe.isFinished()) {
				if (observe.getResCloudlet().getUserId() == getId()) {
					// this is our cloudlet
					if (observe.isExecable()) {
						CustomResCloudlet rcl = observe.getResCloudlet();
//						Log.printLine(getName() + ": WE CAN EXEC THIS CLOUDLET #"+rcl.getCloudletId() );
						sendExecRequest(rcl.getBestDatacenterId(), rcl.getBestVmId(), rcl);
						
					} else {
						Log.printLine(getName() + ": WE NEED HELP FROM PARTNER #"+observe.getResCloudlet().getCloudlet().getCloudletId());
						sendPartnerRequest(observe.getResCloudlet().getCloudlet());
					}

					sendNow(getId(), CloudSimTags.BROKER_ESTIMATE_NEXT_TASK);
				} else {
					// this is partner cloudlet
					sendNow(observe.getResCloudlet().getUserId(), CloudSimTags.PARTNER_ESTIMATE_RETURN, observe.getResCloudlet());
				}
				
				// TODO test
				getEstimationList().remove(re_rcl.getCloudlet());
			}
		}
	}
	
	private void processPartnerCloudletExecRequest(SimEvent ev) {
		CustomResCloudlet rcl = (CustomResCloudlet) ev.getData();
		updatePartnerInformationByValue(ev.getSource(),0,rcl.getCloudletLength());
		sendExecRequest(rcl.getBestDatacenterId(), rcl.getBestVmId(), rcl);
		sendNow(getId(), CloudSimTags.BROKER_ESTIMATE_NEXT_TASK);
	}
	
	private void processPartnerCloudletCancelRequest(SimEvent ev) {
		int[] info = (int[]) ev.getData();
		sendNow(info[1], CloudSimTags.DATACENTER_CANCEL_ESTIMATED_TASK, info[2]);
		sendNow(getId(), CloudSimTags.BROKER_ESTIMATE_NEXT_TASK);
	}
	
	private void sendCancelRequest(int[] cancel_estimate) {
		sendNow(cancel_estimate[0], CloudSimTags.DATACENTER_CANCEL_ESTIMATED_TASK, cancel_estimate[1]);
	}

	private void sendExecRequest(int targetDatacenterId, int vmId, CustomResCloudlet rcl) {
		rcl.getCloudlet().setVmId(vmId);
//		updatePartnerInformation();
		sendNow(targetDatacenterId, CloudSimTags.DATACENTER_EXEC_TASK, vmId);
	}
	
	private void sendPartnerRequest(Cloudlet cloudlet) {
//		Log.printLine(getName() + ": Request help from our partner");
		// TODO calculate ratio
		CustomResCloudlet rcl = new CustomResCloudlet(cloudlet);
		EstimationCloudletOfPartner estPartner = new EstimationCloudletOfPartner(new CustomResCloudlet(cloudlet), 
				new ArrayList<>(getPartnersList()),partnersList);
		getEstimateCloudletofParnerMap().put(cloudlet.getCloudletId(), estPartner);
		
		for (PartnerInfomation pi: estPartner.getPartnerIdsList()) {
//			Log.printLine(getName() + ": Send estimate request to Partner #" + pi.getPartnerId());
			sendNow(pi.getPartnerId(), CloudSimTags.PARTNER_ESTIMATE_REQUEST, rcl);
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
	
	/**
	 * Receive request estimate from partner. send it to add own datacenter to estimate
	 * s
	 */
	@Override
	public void handlerPartnerCloudletEstimateRequest(SimEvent ev){
		CustomResCloudlet crl = (CustomResCloudlet) ev.getData();
		Cloudlet cl = crl.getCloudlet();
//		Log.printLine(CloudSim.clock() + ": " + getName() + ": Received estimate  cloudlet #"+ cl.getCloudletId()+" request from Broker #" + ev.getSource());
		this.addCloudletToEstimationList(cl);
	}
	/**
	 * receive estimate result from partner
	 * @param ev
	 */
	@Override
	protected void processReturnEstimateFromPartner(SimEvent ev) {
		CustomResCloudlet rcl =(CustomResCloudlet) ev.getData();
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Received estimate result from Broker #" + ev.getSource() +"Cloudlet #"+rcl.getCloudletId()+":"+rcl.getBestFinishTime());
		if (getEstimateCloudletofParnerMap().containsKey(rcl.getCloudletId())) {
			EstimationCloudletOfPartner partnerCloudletEstimateList = getEstimateCloudletofParnerMap().get(rcl.getCloudletId());
		
			int[] cancelEstimationResultPartner = partnerCloudletEstimateList.receiveEstimateResult(ev.getSource(), rcl);
			
			if (cancelEstimationResultPartner[0] > -1) {
				sendNow(cancelEstimationResultPartner[0], CloudSimTags.PARTNER_CANCEL_ESTIMATED_TASK, cancelEstimationResultPartner);
			}
			if (partnerCloudletEstimateList.isFinished()) {
				CustomResCloudlet resCloudlet = partnerCloudletEstimateList.getResCloudlet();
				if(partnerCloudletEstimateList.isExecable()){
					Log.printLine(getName() + ": Send Cloudlet #" + resCloudlet.getCloudletId() 
							+ " to Partner #" + partnerCloudletEstimateList.getCurrentBestPartnerId() + " to EXEC");
					PartnerInfomation best  = partnerCloudletEstimateList.getCurrentBestPartner();
					Log.printLine(getName()+" Best K ratio:"+best.getkRatio() + " of cloudlet #" + resCloudlet.getCloudletId()+"is partner #"+partnerCloudletEstimateList.getCurrentBestPartnerId());
					updatePartnerInformation(partnerCloudletEstimateList.getCurrentBestPartner());
					sendNow(partnerCloudletEstimateList.getCurrentBestPartnerId(), CloudSimTags.PARTNER_EXEC, resCloudlet);
				} else {
					Log.printLine(CloudSim.clock()+ " Our partner can not EXEC cloudlet #" + resCloudlet.getCloudletId());
					resCloudlet.setCloudletStatus(Cloudlet.FAILED);
					sendNow(getId(), CloudSimTags.CLOUDLET_RETURN, resCloudlet.getCloudlet());
				}
			}
		} 
		else
		{
			Log.printLine("Error in processReturnEstimateFromPartner, clouled return not exist in list");
		}
	}
	
	private void updatePartnerInformation(PartnerInfomation currentBestPartner) {
		if(currentBestPartner.getPartnerId() == -1){
			return;
		}
		for(PartnerInfomation pInfo :partnersList){
			if(pInfo.getPartnerId() == currentBestPartner.getPartnerId()){
				pInfo.updateLenghtRatio(currentBestPartner.getRequested(), 0);
				pInfo.setRequested(currentBestPartner.getRequested()+pInfo.getRequested());
				pInfo.setkRatio(currentBestPartner.getKRatio());
//				Log.printLine("updated partner info");
//				Log.printLine(pInfo.toString());
			}
		}
		
	}
	
	private void updatePartnerInformationByValue(int partnerId, double request,double satify) {
		for(PartnerInfomation pInfo :partnersList){
			if(pInfo.getPartnerId() == partnerId){
				pInfo.updateRequested(request);
				pInfo.updateSatified(satify);
				pInfo.updateLenghtRatio(0, 0);
				pInfo.updateKRatio();
//				Log.printLine("updated partner info");
//				Log.printLine(pInfo.toString());
			}
		}
		
	}

	public void addDatacenter(int datacenterId) {
		getDatacenterIdsList().add(datacenterId);
	}

	/**
	 * Create list of partner information
	 * @param List o add Entity on system.
	 */
	private void buildPartnerInfoList(List<SimEntity> entityList) {
		int ownVMSize = 0;
		//Get current Databroker
		for(SimEntity en: entityList){
			if (en instanceof DatacenterBroker  && en.getId() == getId()) {
				DatacenterBroker currentBroker =  (DatacenterBroker) en;
				ownVMSize = currentBroker.getVmList().size();
			}
		}
		//build list partner
		for(SimEntity en: entityList){
			if (en instanceof DatacenterBroker  && en.getId() != getId()) {
				DatacenterBroker item = (DatacenterBroker) en;
				int item_size = item.getVmList().size();
				double alphaRatio = (double)ownVMSize/item_size;
//				double alphaRatio = 1;
				Log.printLine("Debug: alpha ratio; "+alphaRatio);
				PartnerInfomation partnerInfoItem   = new PartnerInfomation(en.getId(), alphaRatio);
				Log.printLine(partnerInfoItem.toString());
				this.getPartnersList().add(partnerInfoItem);
			}
			
		}
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

	public Map<Integer, Map<Integer, EstimationCloudletObserve>> getCloudletEstimateObserveMap() {
		return cloudletEstimateObserveMap;
	}

	public void setCloudletEstimateObserveMap(
			Map<Integer, Map<Integer, EstimationCloudletObserve>> cloudletEstimateObserveMap) {
		this.cloudletEstimateObserveMap = cloudletEstimateObserveMap;
	}

	public List<PartnerInfomation> getPartnersList() {
		return partnersList;
	}

	public void setPartnersList(List<PartnerInfomation> partnersList) {
		this.partnersList = partnersList;
	}

	public Map<Integer, EstimationCloudletOfPartner> getEstimateCloudletofParnerMap() {
		return estimateCloudletofParnerMap;
	}

	public void setEstimateCloudletofParnerMap(
			Map<Integer, EstimationCloudletOfPartner> estimateCloudletofParnerMap) {
		this.estimateCloudletofParnerMap = estimateCloudletofParnerMap;
	}

}
