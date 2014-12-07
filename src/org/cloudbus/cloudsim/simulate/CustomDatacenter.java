package org.cloudbus.cloudsim.simulate;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

public class CustomDatacenter extends Datacenter {
	private int datacenterBrokerId;

	public CustomDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
	}
	
	public CustomDatacenter(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval, CustomDatacenterBroker broker) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		setDatacenterBrokerId(broker.getId());		
	}
	
	public void processEvent(SimEvent ev) {
		int srcId = -1;

		switch (ev.getTag()) {
		// Resource characteristics inquiry
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), getCharacteristics());
				break;
			// Resource dynamic info inquiry
			case CloudSimTags.RESOURCE_DYNAMICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), 0);
				break;

			case CloudSimTags.RESOURCE_NUM_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int numPE = getCharacteristics().getNumberOfPes();
				sendNow(srcId, ev.getTag(), numPE);
				break;

			case CloudSimTags.RESOURCE_NUM_FREE_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int freePesNumber = getCharacteristics().getNumberOfFreePes();
				sendNow(srcId, ev.getTag(), freePesNumber);
				break;

			// New Cloudlet arrives
			case CloudSimTags.CLOUDLET_SUBMIT:
				processCloudletSubmit(ev, false);
				break;

			// New Cloudlet arrives, but the sender asks for an ack
			case CloudSimTags.CLOUDLET_SUBMIT_ACK:
				processCloudletSubmit(ev, true);
				break;

			// Cancels a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
				break;

			// Pauses a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
				break;

			// Pauses a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement
			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
				break;

			// Resumes a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_RESUME:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
				break;

			// Resumes a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement
			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE:
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE_ACK:
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE_ACK);
				break;

			// Checks the status of a Cloudlet
			case CloudSimTags.CLOUDLET_STATUS:
				processCloudletStatus(ev);
				break;

			// Ping packet
			case CloudSimTags.INFOPKT_SUBMIT:
				processPingRequest(ev);
				break;

			case CloudSimTags.VM_CREATE:
				processVmCreate(ev, false);
				break;

			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev, true);
				break;

			case CloudSimTags.VM_DESTROY:
				processVmDestroy(ev, false);
				break;

			case CloudSimTags.VM_DESTROY_ACK:
				processVmDestroy(ev, true);
				break;

			case CloudSimTags.VM_MIGRATE:
				processVmMigrate(ev, false);
				break;

			case CloudSimTags.VM_MIGRATE_ACK:
				processVmMigrate(ev, true);
				break;

			case CloudSimTags.VM_DATA_ADD:
				processDataAdd(ev, false);
				break;

			case CloudSimTags.VM_DATA_ADD_ACK:
				processDataAdd(ev, true);
				break;

			case CloudSimTags.VM_DATA_DEL:
				processDataDelete(ev, false);
				break;

			case CloudSimTags.VM_DATA_DEL_ACK:
				processDataDelete(ev, true);
				break;

			case CloudSimTags.VM_DATACENTER_EVENT:
				updateCloudletProcessing();
				checkCloudletCompletion();
				break;
				
			case CloudSimTags.DATACENTER_ESTIMATE_TASK:
				updateCloudletProcessing();
				estimateTask(ev);
				break;
				
			case CloudSimTags.DATACENTER_CANCEL_ESTIMATED_TASK:
				cancelEstimateTask(ev);
				break;
				
			case CloudSimTags.DATACENTER_EXEC_TASK:
				updateCloudletProcessing();
				processTask(ev);
				break;

			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	private void estimateTask(SimEvent ev) {
		CustomResCloudlet rcl = (CustomResCloudlet) ev.getData();
		Cloudlet cl = rcl.getCloudlet();
		
		// time to transfer the files
		double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
		
		double bestFinishTime = Double.MAX_VALUE;
		Vm bestVm = null;
		for (Vm vm: getVmList()) {
			CloudletScheduler scheduler = vm.getCloudletScheduler();
			double mips = vm.getMips();
			double estimatedFinishTime = scheduler.cloudletEstimate(cl, fileTransferTime, mips);
			
			if (estimatedFinishTime < bestFinishTime) {
				vm.getCloudletScheduler().setLastEstimated(null);
				bestFinishTime = estimatedFinishTime;
				bestVm = vm;
				bestVm.getCloudletScheduler().setLastEstimated(rcl);
			}
		}
		
		rcl.setBestFinishTime(bestFinishTime);
		if (bestVm != null) {
			rcl.setBestVmId(bestVm.getId());
		} else {
			rcl.setBestVmId(-1);
		}
		
		sendNow(ev.getSource(), CloudSimTags.BROKER_ESTIMATE_RETURN, rcl);
	}
	
	private void cancelEstimateTask(SimEvent ev) {
		int vmId = (int) ev.getData();
		for (Vm vm: getVmList()) {
			if (vm.getId() == vmId) {
				vm.getCloudletScheduler().setLastEstimated(null);
				break;
			}
		}
	}
	
	@Override
	protected void checkCloudletCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						sendNow(getDatacenterBrokerId(), CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
	}
	
	private void processTask(SimEvent ev) {
		int vmId = (int) ev.getData();
		for (Vm vm: getVmList()) {
			if (vm.getId() == vmId) {
				vm.getCloudletScheduler().getLastEstimated().getCloudlet().setResourceParameter(getId(), 
						getCharacteristics().getCostPerSecond(), getCharacteristics().getCostPerBw());
				double time = vm.getCloudletScheduler().moveEstimatedCloudlet(vm.getId());
				if (time > 0) {
					send(getId(), time, CloudSimTags.VM_DATACENTER_EVENT);
				}
				break;
			}
		}
	}

	public int getDatacenterBrokerId() {
		return datacenterBrokerId;
	}

	public void setDatacenterBrokerId(int datacenterBrokerId) {
		this.datacenterBrokerId = datacenterBrokerId;
	}

}
