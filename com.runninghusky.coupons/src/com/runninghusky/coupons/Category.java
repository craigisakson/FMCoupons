package com.runninghusky.coupons;

public class Category implements Comparable<Category> {
	private String title;
	private String name;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int compareTo(Category category) {
		int result = title.compareTo(category.title);
		return result;
	}

}
