package entities;

import org.codehaus.jackson.annotate.JsonProperty;

public class DeviceIdentity {
	public DeviceIdentity() {
	}
	
	private int ID;

	private String deviceNo;
	
	private String serialNumber;


	public int getID() {
		return ID;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public String getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
}
