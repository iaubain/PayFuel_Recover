package entities;

import org.codehaus.jackson.annotate.JsonProperty;

public class Logged_in_user {
	public Logged_in_user() {
	}
	@JsonProperty("userId")
	 private int user_id;
	@JsonProperty("name")
	 private String name;
	@JsonProperty("branchId")
	 private int branch_id;
	@JsonProperty("branchName")
	 private String branch_name;
	
	 private int logged;
	
	 private String logged_time;
	@JsonProperty("permission")
	 private String permissions;


	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBranch_id() {
		return branch_id;
	}

	public void setBranch_id(int branch_id) {
		this.branch_id = branch_id;
	}

	public String getBranch_name() {
		return branch_name;
	}

	public void setBranch_name(String branch_name) {
		this.branch_name = branch_name;
	}

	public int getLogged() {
		return logged;
	}

	public void setLogged(int logged) {
		this.logged = logged;
	}

	public String getLogged_time() {
		return logged_time;
	}

	public void setLogged_time(String logged_time) {
		this.logged_time = logged_time;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}
}
