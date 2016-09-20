package entities;
public class Pump {
	public Pump() {
	}

	private int pumpId;
	private String pumpName;
	private int branchId;
	private int status;


	public int getPumpId() {
		return pumpId;
	}

	public void setPumpId(int pumpId) {
		this.pumpId = pumpId;
	}

	public String getPumpName() {
		return pumpName;
	}

	public void setPumpName(String pumpName) {
		this.pumpName = pumpName;
	}

	public int getBranchId() {
		return branchId;
	}

	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
