package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.Property;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sun.jna.platform.unix.X11.Window;

public class RealStateExtractor {

	private static ChromeDriver driver;
	private String chromePath; // this is the path to the chrome driver

	public static void main(String[] args) {
		RealStateExtractor e = new RealStateExtractor(
				"C:\\Users\\islam.morad\\Documents\\AngelList\\chromedriver.exe");

		/*
		 * ArrayList<String> urls = e.getPageURLs("New Lambton, NSW 2305",
		 * true);
		 * 
		 * System.out.println("Total URLS generated = " + urls.size());
		 * System.out.println(); System.out.println("The URLs are below:");
		 * 
		 * int index = 1; for (String url : urls) { System.out.println("URL #" +
		 * index + " " + url); index++; }
		 */

		ArrayList<Property> list = e
				.extractURLpage("http://www.realestate.com.au/buy/in-burwood%2c+nsw+2134%3b/list-3");
		System.out.println(list.size() + " propeties extracted");
		System.out.println();
		System.out.println("Propeties details below:");
		System.out.println();
		for (Property property : list) {
			System.out.println(property);
		}

		System.out.println("Closing the driver now.");
		getDriver().quit();
		System.out.println("The driver has been closed.");
	}

	public RealStateExtractor(String chromePath) {
		this.chromePath = chromePath;
		initDriver();
	}

	private void initDriver() {
		System.out.println("driver is being set up...");
		System.setProperty("webdriver.chrome.driver", getChromePath());
		DesiredCapabilities cap = DesiredCapabilities.chrome();
		driver = new ChromeDriver(cap);
		System.out.println("driver is running...");
		System.out.println();
	}

	public ArrayList<String> getPageURLs(String searchQuery, boolean isSale) {
		ArrayList<String> urls = new ArrayList<>();

		if (isSale) {
			searchBuy(searchQuery);
			int pagesCount = getPagesCount();
			System.out.println("Pages count = " + pagesCount);
			for (int i = 1; i <= pagesCount; i++) {
				urls.add(formPageURL(i));
			}
		} else {
			searchRent(searchQuery);
			int pagesCount = getPagesCount();
			System.out.println("Pages count = " + pagesCount);
			for (int i = 1; i <= pagesCount; i++) {
				urls.add(formPageURL(i));
			}
		}

		return urls;
	}

	public ArrayList<Property> extractURLpage(String page_url) {
		ArrayList<Property> properties = new ArrayList<>();

		if (page_url != null && !page_url.isEmpty()) {

			Document document = null;
			try {
				try {
					document = Jsoup
							.connect(page_url)
							.timeout(60 * 1000)
							.referrer("http://www.google.com")
							.validateTLSCertificates(false)
							.userAgent(
									"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36")
							.get();
				} catch (Exception e1) {
					document = Jsoup
							.connect(page_url)
							.timeout(60 * 1000)
							.referrer("http://www.google.com")
							.validateTLSCertificates(false)
							.userAgent(
									"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36")
							.get();
				}

				Element listingsContainer = document.getElementById("results");
				List<Element> listingsList = listingsContainer
						.select("article.resultBody");

				for (Element listing : listingsList) {

					try {
						Element subListingContainer = listing.select(
								"div[class=project-child-listings]").first();
						Elements subLisitngs = subListingContainer.select("a");
						for (Element subListing : subLisitngs) {
							Property subProperty = new Property();
							subProperty.setUrl(subListing.absUrl("href"));

							String address = extractAddress(document, listing);
							subProperty.setAddress(address);

							String price = extractPrice(document, subListing);
							subProperty.setPrice(price);

							int bedsCount = extractBedsCount(document,
									subListing);
							subProperty.setBedsCount(bedsCount);

							int bathsCount = extractBathsCount(document,
									subListing);
							subProperty.setBathsCount(bathsCount);

							int carsCount = extractCarsCount(document,
									subListing);
							subProperty.setCarsCount(carsCount);
							properties.add(subProperty);

						}
						continue;
					} catch (Exception e) {

					}

					String propertyURL = listing.select("div.photoviewer a")
							.first().absUrl("href");
					Property property = new Property();

					if (propertyURL != null && !propertyURL.isEmpty()) {

						property.setUrl(propertyURL);

						String address = extractAddress(document, listing);
						property.setAddress(address);

						String price = extractPrice(document, listing);
						property.setPrice(price);

						int bedsCount = extractBedsCount(document, listing);
						property.setBedsCount(bedsCount);

						int bathsCount = extractBathsCount(document, listing);
						property.setBathsCount(bathsCount);

						int carsCount = extractCarsCount(document, listing);
						property.setCarsCount(carsCount);
						properties.add(property);
					}

				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

		return properties;

	}

	private String extractAddress(Document document, Element listing) {

		try {
			Elements addressElement = document.select("#results_"
					+ listing.attr("id").replaceAll("\\D", "")
					+ " > h2:nth-child(1) > a:nth-child(1)");
			String addressText = addressElement.first().ownText().trim();

			return addressText;
		} catch (Exception e) {
			try {
				Elements addressElement = document.select("#results_"
						+ listing.attr("data-child-listing-id").replaceAll(
								"\\D", "")
						+ " > h2:nth-child(1) > a:nth-child(1)");
				String addressText = addressElement.first().ownText().trim();
				return addressText;
			} catch (Exception e1) {
				System.err.println("No address element to scrape");
				return null;
			}
		}

	}

	private String extractPrice(Document doc, Element listing) {

		try {
			Elements priceElement = listing
					.select("div:nth-child(1) > div:nth-child(1) > p:nth-child(1)");
			String priceText = priceElement.first().text().trim();
			return priceText;
		} catch (Exception e) {

			try {
				Elements priceElement = doc
						.select("#"
								+ listing.attr("id")
								+ " > div:nth-child(3) > div:nth-child(2) > p:nth-child(1)");
				String priceText = priceElement.first().text().trim();
				return priceText;
			} catch (Exception e6) {

				try {
					Elements priceElement = doc
							.select("#"
									+ listing.attr("id")
									+ " > aside:nth-child(3) > div:nth-child(1) > div:nth-child(1) > p:nth-child(1)");
					String priceText = priceElement.first().text().trim();
					return priceText;
				} catch (Exception e1) {

					try {

						Elements priceElement = doc
								.select("#"
										+ listing.attr("id")
										+ " > div:nth-child(3) > div:nth-child(1) > div:nth-child(1)");
						String priceText = priceElement.first().text().trim();
						return priceText;
					} catch (Exception e2) {

						try {

							Elements priceElement = listing
									.select(".repname_viewProjectAddress");
							String priceText = priceElement.first().text()
									.trim();
							return priceText;
						} catch (Exception e3) {
							try {
								// sub listing selector
								Elements priceElement = listing
										.select("div:nth-child(1) > div:nth-child(2) > span:nth-child(1)");
								String priceText = priceElement.first().text()
										.trim();
								return priceText;
							} catch (Exception e4) {
								System.err
										.println("No price element to scrape");
								return null;
							}
						}
					}
				}
			}
		}

	}

	private int extractBedsCount(Document doc, Element listing) {

		try {
			Elements bedsCountElement = listing
					.select("div:nth-child(1) > dl:nth-child(3) > dd:nth-child(2)");
			String bedsCountText = bedsCountElement.first().ownText().trim()
					.replaceAll("\\D", "").trim();
			int bedsCount = Integer.valueOf(bedsCountText);
			return bedsCount;
		} catch (Exception e) {

			try {
				Elements bedsCountElement = doc
						.select("#"
								+ listing.attr("id")
								+ "> div:nth-child(3) > dl:nth-child(4) > dd:nth-child(2)");
				String bedsCountText = bedsCountElement.first().ownText()
						.trim().replaceAll("\\D", "").trim();
				int bedsCount = Integer.valueOf(bedsCountText);
				return bedsCount;
			} catch (Exception e7) {
				try {
					Elements bedsCountElement = doc
							.select("#"
									+ listing.attr("id")
									+ " > aside:nth-child(3) > div:nth-child(1) > dl:nth-child(3) > dd:nth-child(2)");
					String bedsCountText = bedsCountElement.first().ownText()
							.trim().replaceAll("\\D", "").trim();
					int bedsCount = Integer.valueOf(bedsCountText);
					return bedsCount;
				} catch (Exception e1) {
					try {
						Elements bedsCountElement = doc
								.select("#"
										+ listing.attr("id")
										+ " > div:nth-child(3) > div:nth-child(1) > dl:nth-child(3) > dd:nth-child(2)");
						String bedsCountText = bedsCountElement.first()
								.ownText().trim().replaceAll("\\D", "").trim();
						int bedsCount = Integer.valueOf(bedsCountText);
						return bedsCount;
					} catch (Exception e2) {
						// sublisting selector
						try {
							Elements bedsCountElement = listing
									.select("div:nth-child(1) > div:nth-child(3) > dl:nth-child(1) > dd:nth-child(2)");
							String bedsCountText = bedsCountElement.first()
									.ownText().trim().replaceAll("\\D", "")
									.trim();
							int bedsCount = Integer.valueOf(bedsCountText);
							return bedsCount;
						} catch (Exception e3) {
							System.err.println("No beds element to scrape");
							return 0;
						}
					}
				}

			}
		}

	}

	private int extractBathsCount(Document document, Element listing) {

		try {
			Elements bathsCountElement = listing
					.select("div:nth-child(1) > dl:nth-child(3) > dd:nth-child(4)");
			String bathsCountText = bathsCountElement.first().ownText().trim()
					.replaceAll("\\D", "").trim();
			int bathsCount = Integer.valueOf(bathsCountText);
			return bathsCount;
		} catch (Exception e) {
			try {
				Elements bathsCountElement = document
						.select("#"
								+ listing.attr("id")
								+ " > div:nth-child(3) > dl:nth-child(4) > dd:nth-child(4)");
				String bathsCountText = bathsCountElement.first().ownText()
						.trim().replaceAll("\\D", "").trim();
				int bathsCount = Integer.valueOf(bathsCountText);
				return bathsCount;
			} catch (Exception e6) {
				try {
					Elements bathsCountElement = document
							.select("#"
									+ listing.attr("id")
									+ " > aside:nth-child(3) > div:nth-child(1) > dl:nth-child(3) > dd:nth-child(4)");
					String bathsCountText = bathsCountElement.first().ownText()
							.trim().replaceAll("\\D", "").trim();
					int bathsCount = Integer.valueOf(bathsCountText);
					return bathsCount;
				} catch (Exception e1) {

					try {
						Elements bathsCountElement = document
								.select("#"
										+ listing.attr("id")
										+ " > div:nth-child(3) > div:nth-child(1) > dl:nth-child(3) > dd:nth-child(4)");
						String bathsCountText = bathsCountElement.first()
								.ownText().trim().replaceAll("\\D", "").trim();
						int bathsCount = Integer.valueOf(bathsCountText);
						return bathsCount;
					} catch (Exception e2) {
						try {
							// sub listing selector
							Elements bathsCountElement = listing
									.select("div:nth-child(1) > div:nth-child(3) > dl:nth-child(1) > dd:nth-child(4)");
							String bathsCountText = bathsCountElement.first()
									.ownText().trim().replaceAll("\\D", "")
									.trim();
							int bathsCount = Integer.valueOf(bathsCountText);
							return bathsCount;
						} catch (Exception e3) {
							System.err.println("No baths element to scrape");
							return 0;
						}
					}
				}
			}
		}

	}

	private int extractCarsCount(Document document, Element listing) {
		try {
			Elements carsCountElement = listing
					.select("div:nth-child(1) > dl:nth-child(3) > dd:nth-child(6)");
			String carsCountText = carsCountElement.first().ownText()

			.trim().replaceAll("\\D", "").trim();
			int carsCount = Integer.valueOf(carsCountText);
			return carsCount;
		} catch (Exception e) {
			try {
				Elements carsCountElement = document
						.select("#"
								+ listing.attr("id")
								+ " > div:nth-child(3) > dl:nth-child(4) > dd:nth-child(6)");
				String carsCountText = carsCountElement.first().ownText()

				.trim().replaceAll("\\D", "").trim();
				int carsCount = Integer.valueOf(carsCountText);
				return carsCount;
			} catch (Exception e7) {
				try {
					Elements carsCountElement = document
							.select("#"
									+ listing.attr("id")
									+ " > aside:nth-child(3) > div:nth-child(1) > dl:nth-child(3) > dd:nth-child(6)");
					String carsCountText = carsCountElement.first().ownText()

					.trim().replaceAll("\\D", "").trim();
					int carsCount = Integer.valueOf(carsCountText);
					return carsCount;
				} catch (Exception e1) {
					try {
						Elements carsCountElement = document
								.select("#"
										+ listing.attr("id")
										+ " > div:nth-child(3) > div:nth-child(1) > dl:nth-child(3) > dd:nth-child(6)");
						String carsCountText = carsCountElement.first()
								.ownText()

								.trim().replaceAll("\\D", "").trim();
						int carsCount = Integer.valueOf(carsCountText);
						return carsCount;
					} catch (Exception e2) {
						try {
							// sublisting selector
							Elements carsCountElement = listing
									.select("div:nth-child(1) > div:nth-child(3) > dl:nth-child(1) > dd:nth-child(6)");
							String carsCountText = carsCountElement.first()
									.ownText().trim().replaceAll("\\D", "")
									.trim();
							int carsCount = Integer.valueOf(carsCountText);
							return carsCount;
						} catch (Exception e3) {
							System.err.println("No cars element to scrape");
							return 0;
						}
					}
				}
			}
		}
	}

	/*
	 * public ArrayList<Property> extractURLpage(String page_url) {
	 * WebDriverWait wait = new WebDriverWait(driver, 15); ArrayList<Property>
	 * properties = new ArrayList<>();
	 * 
	 * if (page_url != null && !page_url.isEmpty()) { driver.get(page_url);
	 * WebElement listingsContainer = wait.until(ExpectedConditions
	 * .visibilityOfElementLocated(By.id("results"))); List<WebElement>
	 * listingsList = listingsContainer.findElements(By
	 * .cssSelector("article.resultBody"));
	 * 
	 * for (WebElement listing : listingsList) { String propertyURL =
	 * listing.findElement( By.cssSelector("div.photoviewer a")).getAttribute(
	 * "href"); if (propertyURL != null && !propertyURL.isEmpty()) { Property
	 * property = new Property(); property.setUrl(propertyURL);
	 * 
	 * try { WebElement addressElement = driver
	 * .findElementByCssSelector("#results_" + listing.getAttribute("id") +
	 * " > h2 > a"); String addressText = addressElement.getText().trim();
	 * property.setAddress(addressText); } catch (Exception e) {
	 * System.err.println("No beds element to scrape"); }
	 * 
	 * try { WebElement priceElement = driver .findElementByCssSelector("#" +
	 * listing.getAttribute("id") +
	 * " > div.listing-content.resultBodyWrapper > div > div.propertyStats > p.priceText"
	 * ); String priceText = priceElement.getText().trim();
	 * property.setPrice(priceText); } catch (Exception e) { try { WebElement
	 * priceElement = driver .findElementByCssSelector("#" +
	 * listing.getAttribute("id") +
	 * " > div.listingInfo.rui-clearfix > div.propertyStats"); String priceText
	 * = priceElement.getText().trim(); property.setPrice(priceText); } catch
	 * (Exception e1) { System.err.println("No price element to scrape"); } }
	 * 
	 * try { WebElement bedsCountElement = driver .findElementByCssSelector("#"
	 * + listing.getAttribute("id") + " > aside > div > dl > dd:nth-child(2)");
	 * String bedsCountText =
	 * bedsCountElement.getText().trim().replaceAll("\\D", "").trim(); int
	 * bedsCount = Integer.valueOf(bedsCountText);
	 * property.setBedsCount(bedsCount); } catch (Exception e) {
	 * System.err.println("No beds element to scrape"); }
	 * 
	 * try { WebElement bathsCountElement = driver .findElementByCssSelector("#"
	 * + listing.getAttribute("id") + " > aside > div > dl > dd:nth-child(4)");
	 * String bathsCountText =
	 * bathsCountElement.getText().trim().replaceAll("\\D", "").trim(); int
	 * bathsCount = Integer.valueOf(bathsCountText);
	 * property.setBathsCount(bathsCount); } catch (Exception e) {
	 * System.err.println("No cars element to scrape"); }
	 * 
	 * try { WebElement carsCountElement = driver .findElementByCssSelector("#"
	 * + listing.getAttribute("id") + " > aside > div > dl > dd:nth-child(6)");
	 * String carsCountText =
	 * carsCountElement.getText().trim().replaceAll("\\D", "").trim(); int
	 * carsCount = Integer.valueOf(carsCountText);
	 * property.setCarsCount(carsCount); } catch (Exception e) {
	 * System.err.println("No cars element to scrape"); }
	 * 
	 * } }
	 * 
	 * }
	 * 
	 * return properties;
	 * 
	 * }
	 */

	// do search in the buy section
	private void searchBuy(String query) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		driver.navigate().to("https://www.realestate.com.au/buy");
		WebElement surroundingSuburbs = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.id("includeSurrounding")));
		if (surroundingSuburbs.isSelected()) {
			surroundingSuburbs.click();
		}

		if (query != null && !query.isEmpty()) {
			WebElement searchBox = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.id("where")));
			searchBox.sendKeys(query);
			WebElement searchButton = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By
							.className("rui-search-button")));
			searchButton.click();
		} else {
			System.err.println("You must provide a search query.");
		}
	}

	// do search in the rent section
	private void searchRent(String query) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		driver.navigate().to("https://www.realestate.com.au/rent");
		WebElement surroundingSuburbs = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.id("includeSurrounding")));
		if (surroundingSuburbs.isSelected()) {
			surroundingSuburbs.click();
		}

		if (query != null && !query.isEmpty()) {
			WebElement searchBox = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.id("where")));
			searchBox.sendKeys(query);
			WebElement searchButton = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By
							.className("rui-search-button")));
			searchButton.click();
		} else {
			System.err.println("You must provide a search query.");
		}
	}

	private int getPagesCount() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			WebElement paginationTextElement = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By
							.cssSelector("#resultsInfo > p")));
			String text = paginationTextElement.getText();

			int start = text.indexOf("of") + 2;
			int end = text.indexOf("total");

			String countText = text.substring(start, end).replaceAll("\\D", "");

			double pagesCount = Double.valueOf(countText) / 20; // 20 is the
																// number of
																// listings for
																// each page.
			return round(pagesCount);
		} catch (NumberFormatException e) {
			System.err.println("Cannot convert String to int.");
			return 0;
		} catch (Exception e) {
			return 1;
		}
	}

	private String formPageURL(int pageNum) {
		String currentUrl = driver.getCurrentUrl();
		int end = currentUrl.indexOf("list-");
		String next = currentUrl.substring(0, end) + "list-" + pageNum
				+ "?includeSurrounding=false";
		return next;
	}

	// This is to round a double number
	private int round(double d) {
		double dAbs = Math.abs(d);
		int i = (int) dAbs;
		double result = dAbs - (double) i;
		if (result <= 0) {
			return d < 0 ? -i : i;
		} else {
			return d < 0 ? -(i + 1) : i + 1;
		}
	}

	// Please ignore the below for now.

	// ------------------------- Other functionality that we may use later
	// -----------------\\

	// Navigate to the next page.
	private void goToNextPage(int pageNum) {
		String currentUrl = driver.getCurrentUrl();
		int end = currentUrl.indexOf("list-");
		String next = currentUrl.substring(0, end) + "list-" + pageNum
				+ "?includeSurrounding=false";
		driver.navigate().to(next);
	}

	private Property extractProperty(String propertyURL) {
		Property property = new Property();

		if (propertyURL != null && !propertyURL.isEmpty()) {

			String parentWidnow = driver.getWindowHandle();
			((JavascriptExecutor) driver).executeScript("window.open('"
					+ propertyURL + "', '_blank');");
			Set<String> windowHandles = driver.getWindowHandles();
			for (String windowHandle : windowHandles) {
				if (!windowHandle.equals(parentWidnow)) {
					driver.switchTo().window(windowHandle);

					property.setUrl(driver.getCurrentUrl());

					try {
						WebElement priceElement = driver
								.findElementByCssSelector("#listing_info > ul > li.price > p.priceText");
						String price = priceElement.getText().trim();
						property.setPrice(price);
					} catch (Exception e) {
						try {
							WebElement priceElement = driver
									.findElementByCssSelector("#listing_info > ul > li.price");
							String price = priceElement.getText().trim();
							property.setPrice(price);
						} catch (Exception e1) {
							System.err.println("No price element to scrape");
							// e1.printStackTrace();
						}
					}

					try {
						WebElement addressElement = driver
								.findElementByCssSelector("#listing_header > h1[itemprop=address]");
						String address = addressElement.getText().trim();
						property.setAddress(address);
					} catch (Exception e) {
						System.err.println("No address element to scrape");
						e.printStackTrace();
					}

					try {
						WebElement bedsCountElement = driver
								.findElementByCssSelector("#features > div:nth-child(1) > div > ul:nth-child(1) > li:nth-child(3)");
						String bedsCountText = bedsCountElement.getText()
								.trim();
						int bedsCount = Integer.valueOf(bedsCountText
								.replaceAll("\\D", "").trim());
						property.setBedsCount(bedsCount);
					} catch (Exception e) {
						System.err.println("No beds element to scrape");
						e.printStackTrace();
					}

					try {
						WebElement bathsCountElement = driver
								.findElementByCssSelector("#features > div:nth-child(1) > div > ul:nth-child(1) > li:nth-child(4)");
						String bathsCountText = bathsCountElement.getText()
								.trim();
						int bedsCount = Integer.valueOf(bathsCountText
								.replaceAll("\\D", "").trim());
						property.setBathsCount(bedsCount);
					} catch (Exception e) {
						System.err.println("No baths element to scrape");
						e.printStackTrace();
					}

					try {
						WebElement carsElement = driver
								.findElementByCssSelector("#features > div.featureListWrapper.last > div > ul:nth-child(1) > li:nth-child(2)");
						String carsCountText = carsElement.getText().trim();
						int carsCount = Integer.valueOf(carsCountText
								.replaceAll("\\D", "").trim());
						property.setCarsCount(carsCount);
					} catch (Exception e) {
						System.err.println("No cars element to scrape");
						e.printStackTrace();
					}

					driver.close();
					driver.switchTo().window(parentWidnow);
				}
			}

			return property;
		} else {
			return null;
		}
	}

	public ArrayList<String> getListingsURLs(String searchQuery, boolean isSale) {
		ArrayList<String> urls = new ArrayList<>();

		if (isSale) {
			searchBuy(searchQuery);
			int pagesCount = getPagesCount();
			System.out.println("Pages count = " + pagesCount);
			System.out.println("Started extracting the URLs");
			urls = extractLinks(pagesCount);
			System.out.println("Extraction DONE.");
		} else {
			searchRent(searchQuery);
			int pagesCount = getPagesCount();
			System.out.println("Pages count = " + pagesCount);
			System.out.println("Started extracting the URLs");
			urls = extractLinks(pagesCount);
			System.out.println("Extraction DONE.");
		}

		return urls;
	}

	private ArrayList<String> extractLinks(int pagesCount) {
		WebDriverWait wait = new WebDriverWait(driver, 15);
		ArrayList<String> urls = new ArrayList<>();
		if (pagesCount == 0) {
			return null;
		} else {
			int index = 1;
			while (index <= pagesCount) {
				WebElement listingsContainer = wait.until(ExpectedConditions
						.visibilityOfElementLocated(By.id("results")));
				List<WebElement> listingsList = listingsContainer
						.findElements(By.cssSelector("article.resultBody"));
				System.out.println("Page #" + index + " Found "
						+ listingsList.size() + " listings");
				for (WebElement listing : listingsList) {
					String url = listing.findElement(
							By.cssSelector("div.photoviewer a")).getAttribute(
							"href");
					if (url != null && !url.isEmpty()) {
						urls.add(url);
					}
				}

				index++;
				if (index > pagesCount) {
					break;
				} else {
					goToNextPage(index);
					// waiting is mandatory for we don't get banned. The more we
					// wait the better.
					waitFor15Secs();
				}

			}
		}
		return urls;
	}

	private void waitFor15Secs() {
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getChromePath() {
		return chromePath;
	}

	public void setChromePath(String chromePath) {
		this.chromePath = chromePath;
	}

	public static ChromeDriver getDriver() {
		return driver;
	}

}