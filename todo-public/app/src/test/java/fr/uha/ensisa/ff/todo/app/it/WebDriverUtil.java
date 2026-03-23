package fr.uha.ensisa.ff.todo.app.it;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebDriverUtil {

	public static WebDriver driver;
	private static String port, name;

	private static void findGeckodriver() {
		String ext = System.getProperty("os.name", "").toLowerCase().startsWith("win") ? ".exe" : "";
		String geckodrivername = "geckodriver" + ext;
			
		Collection<String> pathes = new ArrayList<>();
		for (String source : new String [] {System.getProperty("PATH"), System.getenv().get("Path")}) {
			if (source != null) {
				pathes.addAll(Arrays.asList(source.trim().split(File.pathSeparator)));
			}
		}
		for (String path : pathes) {
			File f = new File(path, geckodrivername);
			if (f.exists() && f.canExecute()) {
				System.setProperty("webdriver.gecko.driver", f.getAbsolutePath());
				return;
			}
		}
		for (String path : System.getProperty("PATH", "").split(File.pathSeparator)) {
			File f = new File(path, geckodrivername);
			if (f.exists() && f.canExecute()) {
				System.setProperty("webdriver.gecko.driver", f.getAbsolutePath());
				return;
			}
		}
	}

	static void init() {
		if (driver != null) return;

		findGeckodriver();
		driver = new FirefoxDriver();
		
		port = System.getProperty("servlet.port", "8080");
		// driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		System.out.println(
				"Configured app " + name + " on port " + port + " with driver " + driver.getClass().getSimpleName());
	}

	static void close() {
		if (driver == null)
			return;
		driver.quit();
		try {
			driver.close();
		} catch (Exception x) {
		}
		driver = null;
	}

	public static String getBaseUrl() {
		return "http://localhost:" + port;
	}
}
