package org.cloudbus.cloudsim.simulate;

import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * A simple example showing how to create a data center with one host and run
 * one cloudlet on it.
 */
public class Simulate {
	
	private static final String filePath = "home/ngtrieuvi92/zzzz/simulator/src/org/cloudbus/cloudsim/simulate/testcase.json";
	
	private static List<DatacenterBroker> brokersList;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args
	 *            the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample1...");
		brokersList = new ArrayList<DatacenterBroker>();

		try {
			// Initialize CloudSim
			int num_user = 1;
			Calendar calendar = Calendar.getInstance(); 
			boolean trace_flag = false;
			CloudSim.init(num_user, calendar, trace_flag);
			
			
			// Read data from json file
			FileReader reader = new FileReader(filePath);

            JSONParser jsonParser = new JSONParser();
            JSONArray members = (JSONArray) jsonParser.parse(reader);
            
            // Create Datacenterbrokers
            for (int i = 0; i < members.size(); i++) {
            	JSONObject member = (JSONObject) members.get(i);
            	
            	String m_name = (String) member.get("name");
            	Log.printLine(m_name);
            	CustomDatacenterBroker broker = createBroker(m_name);
            	brokersList.add(broker);
            	
            	List<Vm> vmList = new ArrayList<Vm>();
            	List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
            	
            	// Create datacenters
            	JSONObject m_datacenters = (JSONObject) member.get("datacenters");
            	createDatacenter(m_datacenters, broker);
            	
            	JSONObject m_vm = (JSONObject) m_datacenters.get("vms");
            	createVm(vmList, m_vm, broker);
            	broker.submitVmList(vmList);
            	
            	JSONObject m_cloudlet = (JSONObject) member.get("cloudlets");
            	if (m_cloudlet == null) {
            		Log.printLine(broker.getName() + ": There is no cloudlet");
            		continue;
            	}
            	
            	int cloudlet_quantity = ((Long) m_cloudlet.get("quantity")).intValue();
            	int cloudletId_prefix = broker.getId() * 1000;
            	
        		long length = (Long) m_cloudlet.get("long");
        		long fileSize = (Long) m_cloudlet.get("fileSize");
        		long outputSize = (Long) m_cloudlet.get("outputSize");
        		int pesNumber = ((Long) m_cloudlet.get("pesNumber")).intValue();
        		double deadline = (Double) m_cloudlet.get("deadline");
            	
            	for (int j = 0; j < cloudlet_quantity; j++) {          		
            		UtilizationModel utilizationModel = new UtilizationModelFull();
            		
            		Cloudlet cloudlet = new Cloudlet(cloudletId_prefix + j, length, pesNumber, fileSize, outputSize, 
            				utilizationModel, utilizationModel, utilizationModel);
            		cloudlet.setUserId(broker.getId());
            		cloudletList.add(cloudlet);
            		
            		Log.printLine("Cloudlet #" + cloudlet.getCloudletId() 
            				+ " has been created and is belonged to Broker #" + cloudlet.getUserId());
            	}

            	broker.submitCloudletList(cloudletList);
            }
            
            
			CloudSim.startSimulation();
			CloudSim.stopSimulation();
			
			for (DatacenterBroker broker : brokersList) {
				List<Cloudlet> newList = broker.getCloudletReceivedList();
				printCloudletList(newList);
			}
				
			Log.printLine("CloudSimExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	
	private static void createDatacenter(JSONObject d_info, CustomDatacenterBroker broker) {
		String name = broker.getName();
		Log.printLine(name + ": creating datacenters");
		
		int datacenter_quantity = ((Long) d_info.get("quantity")).intValue();
		
		List<Host> hostList = new ArrayList<Host>();
		
		JSONObject hosts = (JSONObject) d_info.get("hosts");
		int hosts_quantity = ((Long) hosts.get("quantity")).intValue();
		for (int i = 0; i < hosts_quantity; i++) {
			int ram = ((Long) hosts.get("ram")).intValue();
			long storage = (Long) hosts.get("storage");
			int bw = ((Long) hosts.get("bw")).intValue();
			
			JSONObject pes = (JSONObject) hosts.get("pes");
			int pes_quantity = ((Long) pes.get("quantity")).intValue();
			long pes_mips = (Long) pes.get("mips");
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < pes_quantity; j++) {
				peList.add(new Pe(j, new PeProvisionerSimple(pes_mips)));
			}
			
			hostList.add(new Host(i, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), 
					storage , peList, new VmSchedulerTimeShared(peList)));
		}
		
		String arch = (String) d_info.get("arch");
		String os = (String) d_info.get("os");
		String vmm = (String) d_info.get("vmm");
		double time_zone = (double) d_info.get("time_zone");
		double cost = (double) d_info.get("cost");
		double costPerMem = (double) d_info.get("costPerMem");
		double costPerStorage = (double) d_info.get("costPerStorage");
		double costPerBw = (double) d_info.get("costPerBw");
		
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a Datacenter object.
		for (int i = 0; i < datacenter_quantity; i++) {
			try {
				CustomDatacenter datacenter = new CustomDatacenter(name, characteristics,
						new VmAllocationPolicySimple(hostList), storageList, 0, broker);
				
				broker.addDatacenter(datacenter.getId());
				
				Log.printLine("Datacenter #" + datacenter.getId() 
						+ " has been created and is belonged to Broker #" + datacenter.getDatacenterBrokerId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void createVm(List<Vm> vmList, JSONObject m_vm, CustomDatacenterBroker broker) {
		String brokerName = broker.getName();
		int vm_quantity = ((Long) m_vm.get("quantity")).intValue();
		int vmId_prefix = broker.getId() * 1000;
		for (int i = 0; i < vm_quantity; i++) {
			int mips = ((Long) m_vm.get("mips")).intValue();
			long size = (Long) m_vm.get("size"); // image size (MB)
			int ram = ((Long) m_vm.get("ram")).intValue(); // vm memory (MB)
			long bw = (Long) m_vm.get("bw");
			int pesNumber = ((Long) m_vm.get("pesNumber")).intValue(); // number of cpus
			String vmm = "Xen"; // VMM name
			int vmId = vmId_prefix + i;

			// create VM
			Vm vm = new Vm(vmId, broker.getId(), mips, pesNumber, ram, bw, size, vmm,
					new CloudletSchedulerSpaceShared());
			
			vmList.add(vm);

			Log.printLine(brokerName + ": creating Vm #" + vmId);
		}
	}

	
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static CustomDatacenterBroker createBroker(String name) {
		CustomDatacenterBroker broker = null;
		try {
			broker = new CustomDatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list
	 *            list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}
}
