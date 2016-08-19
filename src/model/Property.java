package model;

public class Property {

	private String price;
	private String address;
	private String url;
	private int carsCount;
	private int bathsCount;
	private int bedsCount;

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getCarsCount() {
		return carsCount;
	}

	public void setCarsCount(int carsCount) {
		this.carsCount = carsCount;
	}

	public int getBathsCount() {
		return bathsCount;
	}

	public void setBathsCount(int bathsCount) {
		this.bathsCount = bathsCount;
	}

	public int getBedsCount() {
		return bedsCount;
	}

	public void setBedsCount(int bedsCount) {
		this.bedsCount = bedsCount;
	}

	@Override
	public String toString() {
		return "Property [price=" + price + ", address=" + address + ", url="
				+ url + ", carsCount=" + carsCount + ", bathsCount="
				+ bathsCount + ", bedsCount=" + bedsCount + "]";
	}
}
