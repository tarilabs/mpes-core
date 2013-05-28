package net.tarilabs.mpes.model;

public class CurStatusPrevStatus {
	private String id;
	private String curStatus;
	private Long curStatusTs;
	private String prevStatus;
	private Long prevStatusTs;
	
	public CurStatusPrevStatus(String id) {
		super();
		this.id = id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CurStatusPrevStatus [id=").append(id)
				.append(", curStatus=").append(curStatus)
				.append(", curStatusTs=").append(curStatusTs)
				.append(", prevStatus=").append(prevStatus)
				.append(", prevStatusTs=").append(prevStatusTs).append("]");
		return builder.toString();
	}

	public String getCurStatus() {
		return curStatus;
	}

	public void setCurStatus(String curStatus) {
		this.curStatus = curStatus;
	}

	public Long getCurStatusTs() {
		return curStatusTs;
	}

	public void setCurStatusTs(Long curStatusTs) {
		this.curStatusTs = curStatusTs;
	}

	public String getPrevStatus() {
		return prevStatus;
	}

	public void setPrevStatus(String prevStatus) {
		this.prevStatus = prevStatus;
	}

	public Long getPrevStatusTs() {
		return prevStatusTs;
	}

	public void setPrevStatusTs(Long prevStatusTs) {
		this.prevStatusTs = prevStatusTs;
	}

	public String getId() {
		return id;
	}
	
	public void shiftCurIntoPrev() {
		prevStatus = curStatus;
		prevStatusTs = curStatusTs;
	}
}
