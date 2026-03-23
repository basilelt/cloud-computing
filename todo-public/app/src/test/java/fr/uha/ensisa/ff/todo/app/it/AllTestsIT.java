package fr.uha.ensisa.ff.todo.app.it;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TaskITPart.class
})
public class AllTestsIT {
	
	@BeforeClass
	public static void setupWebDriver() {
		WebDriverUtil.init();
	}
	
	@AfterClass
	public static void shutdownWebDriver() {
		WebDriverUtil.close();
	}

}
