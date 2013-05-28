package net.tarilabs.mpes.model;

public class MpesSentence {
	String area;
	String message;
	
	public MpesSentence(String area, String message) {
		super();
		this.area = area;
		this.message = message;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MpesSentence [area=").append(area).append(", message=")
				.append(message).append("]");
		return builder.toString();
	}
	
	
}
