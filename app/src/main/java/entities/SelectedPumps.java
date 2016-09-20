package entities;
public class SelectedPumps {
	public SelectedPumps() {
	}

	private int id;
	private int nozzleId;
	private int userId;


	public int getNozzleId() {
		return nozzleId;
	}

	public void setNozzleId(int nozzleId) {
		this.nozzleId = nozzleId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
