package com.runninghusky.coupons;

public class Coupon implements Comparable<Coupon>{
	private String imageUri;
	private String retailer;
	private String id;
	private String heading;
	private String cat;

	public String getImageUri() {
		return imageUri;
	}

	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}

	public String getRetailer() {
		return retailer;
	}

	public void setRetailer(String retailer) {
		this.retailer = retailer;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHeading() {
		return heading;
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public Coupon() {

	}

	public int compareTo(Coupon coupon) {
		int result = retailer.compareTo(coupon.retailer);
		return result;
	}

}
