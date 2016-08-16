package controller;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RealStateExtractor {

	private static ChromeDriver driver;
	private String chromePath; // this is the path to the chrome driver

	public static void main(String[] args) {
		RealStateExtractor e = new RealStateExtractor(
				"C:\\Users\\islam.morad\\Documents\\AngelList\\chromedriver.exe");
		
		ArrayList<String> urls = e.getPageURLs("New Lambton, NSW 2305", true);
		
		System.out.println("Total URLS generated = " + urls.size());
		System.out.println();
		System.out.println("The URLs are below:");
		
		int index = 1;
		for (String url : urls) {
			System.out.println("URL #" + index + " " + url);
			index++;
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
			for(int i = 1; i <= pagesCount; i++){
				urls.add(formPageURL(i));
			}
		} else {
			searchRent(searchQuery);
			int pagesCount = getPagesCount();
			System.out.println("Pages count = " + pagesCount);
			for(int i = 1; i <= pagesCount; i++){
				urls.add(formPageURL(i));
			}
		}

		return urls;
	}
	
	

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
			System.err.println("Cannot converst String to int.");
			return 0;
		} catch (Exception e) {
			return 1;
		}
	}
	
	private String formPageURL(int pageNum){
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
	
	//Please ignore the below for now.
	
	//------------------------- Other functionality that we may use later -----------------\\

	// Navigate to the next page.
	private void goToNextPage(int pageNum) {
		String currentUrl = driver.getCurrentUrl();
		int end = currentUrl.indexOf("list-");
		String next = currentUrl.substring(0, end) + "list-" + pageNum
				+ "?includeSurrounding=false";
		driver.navigate().to(next);
	}
	
	public ArrayList<String> getListingsURLs(String searchQuery, boolean isSale){
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
				System.out.println("Page #"+ index +" Found " + listingsList.size() + " listings");
				for (WebElement listing : listingsList) {
					String url = listing.findElement(By.cssSelector("div.photoviewer a"))
							.getAttribute("href");
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