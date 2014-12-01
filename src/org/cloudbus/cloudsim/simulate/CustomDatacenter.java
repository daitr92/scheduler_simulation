package org.cloudbus.cloudsim.simulate;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;

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

	public int getDatacenterBrokerId() {
		return datacenterBrokerId;
	}

	public void setDatacenterBrokerId(int datacenterBrokerId) {
		this.datacenterBrokerId = datacenterBrokerId;
	}

}
