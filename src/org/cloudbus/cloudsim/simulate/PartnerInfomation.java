package org.cloudbus.cloudsim.simulate;

public class PartnerInfomation {

	private int partnerId;
	
	private double ratio;
	
	private double requested;
	
	private double satified;

	
	public PartnerInfomation(int partnerId, double ratio, double requested,
			double satified) {
		super();
		this.partnerId = partnerId;
		this.ratio = ratio;
		this.requested = requested;
		this.satified = satified;
	}

	public PartnerInfomation(int partnerId) {
		super();
		this.partnerId = partnerId;
		this.ratio = 1;
		this.requested = 0;
		this.satified = 0;
	}
	
	public PartnerInfomation(int partnerId, double ratio) {
		super();
		this.partnerId = partnerId;
		this.ratio = ratio;
		this.requested = 0;
		this.satified = 0;
	}


	@Override
	public String toString() {
		return "PartnerInfomation [partnerId=" + partnerId + ", ratio=" + ratio
				+ ", requested=" + requested + ", satified=" + satified + "]";
	}

	public int getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public double getRequested() {
		return requested;
	}

	public void setRequested(double requested) {
		this.requested = requested;
	}

	public double getSatified() {
		return satified;
	}

	public void setSatified(double satified) {
		this.satified = satified;
	}

}
