package org.cloudbus.cloudsim.simulate;

import java.util.Comparator;

import org.cloudbus.cloudsim.Cloudlet;

public class CloudletComparator implements Comparator<Cloudlet> {

	@Override
	public int compare(Cloudlet cl1, Cloudlet cl2) {
		return (int)((cl1.getUserRequestTime() - cl2.getUserRequestTime()) * 10);
	}

}
