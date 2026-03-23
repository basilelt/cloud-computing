package fr.uha.ensisa.ff.todo.app.it;

import static fr.uha.ensisa.ff.todo.app.it.WebDriverUtil.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import fr.uha.ensisa.ff.todo.Task;

public class TaskITPart {

	public static final String testUser = "test user";
	public static final String editTaskPrefix = "edit_task_";
	public static final String deleteTaskPrefix = "delete_task_";
	
	@AfterClass
	public static void deleteAllTestsStatic() {
		goToListPage();
		do {
			List<WebElement> tasksDelElts;
			try {
				tasksDelElts = driver.findElements(By.cssSelector("#tasks [id^=" + deleteTaskPrefix + "]"));
			} catch (InvalidSelectorException x) {
				return;
			} catch (Exception x) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				tasksDelElts = driver.findElements(By.cssSelector("#tasks [id^=" + deleteTaskPrefix + "]"));
			}
			if (tasksDelElts.isEmpty()) return;
			try {
				tasksDelElts.get(0).click();
			} catch (Exception x) {}
		} while (true);
	}
	
	public boolean loginIfNecessary(String user) {
		goToListPage();
		// Tries to find the logout button
		WebElement logoutBtn;
		try {
			logoutBtn = driver.findElement(By.id("disconnect"));
		} catch (NoSuchElementException x) {
			logoutBtn = null;
		}
		if (logoutBtn != null) {
			String loggedUser = getLoggedName();
			assertNotNull(loggedUser);
			if (user.equals(loggedUser)) return false;
			logoutBtn.click();
		}
		assertNull(getLoggedName());
		// The form name
		WebElement loginForm = driver.findElement(By.name("name"));
		loginForm.sendKeys(user);
		loginForm.submit();
		// Waiting for page to load
		(new WebDriverWait(driver, Duration.ofSeconds(1))).until(ExpectedConditions.presenceOfElementLocated(By.id("loggedName")));
		assertEquals(user, getLoggedName());
		return true;
	}
	
	public String getLoggedName() {
		try {
			WebElement nameElt = driver.findElement(By.id("loggedName"));
			return nameElt.getText();
		} catch (NoSuchElementException x) {
			return null;
		}
	}
	
	public boolean logoutIfPossible() {
		goToListPage();
		WebElement logoutBtn;
		try {
			logoutBtn = driver.findElement(By.id("disconnect"));
		} catch (NoSuchElementException x) {
			logoutBtn = null;
		}
		if (logoutBtn == null) return false;
		assertNotNull(getLoggedName());
		logoutBtn.click();
		assertNull(getLoggedName());
		return true;
	}

	private static void goToListPage() {
		driver.get(getBaseUrl());
	}
	
	@Before
	public void deleteAllTests() {
		this.loginIfNecessary(testUser);
		deleteAllTestsStatic();
	}
	
	public void deleteTest(Long id) {
		goToListPage();
		if (id == null) return;
		try {
			WebElement tasksDelElt = driver.findElement(By.id(deleteTaskPrefix + id));
			tasksDelElt.click();
		} catch (NoSuchElementException x) {}
	}

	public List<Task> getTests() {
		goToListPage();
		try {
			List<WebElement> tasksElts = driver.findElements(By.cssSelector("#tasks [id^=\"" + editTaskPrefix + "\"]"));
			List<Task> ret = new ArrayList<>(tasksElts.size());
			for (WebElement taskElt : tasksElts) {
				String idStr = taskElt.getAttribute("id").trim().substring(editTaskPrefix.length());
				long id = Long.parseLong(idStr);
				String name = taskElt.getText();
				Task t = new Task();
				t.setId(id);
				t.setName(name);
				ret.add(t);
			}
			return ret;
		} catch (InvalidSelectorException x) {
			return Collections.emptyList();
		}
	}
	
	public List<Long> getTestIds() {
		List<Task> res = getTests();
		List<Long> ret = new ArrayList<>(res.size());
		for (Task task : res) {
			ret.add(task.getId());
		}
		return ret;
	}
	
	public List<String> getTestNames() {
		List<Task> res = getTests();
		List<String> ret = new ArrayList<>(res.size());
		for (Task task : res) {
			ret.add(task.getName());
		}
		return ret;
	}
	
	public long createTestAndGetId(String name) {
		List<Long> initialTasks = getTestIds();
		createTest(name);
		List<Long> finalTasks = getTestIds();
		finalTasks.removeAll(initialTasks);
		assertEquals("Created a test " + name + " but could not find it in the list (got a diff of " + finalTasks + ')', 1, finalTasks.size());
		return finalTasks.get(0);
	}

	private void createTest(String name) {
		WebElement createNewBtn;
		try {
			createNewBtn= driver.findElement(By.id("new_task"));
		} catch (Exception x) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			createNewBtn= driver.findElement(By.id("new_task"));
		}
		createNewBtn.click();
		WebElement nameInput;
		try {
			nameInput = driver.findElement(By.name("name"));
		} catch (Exception x) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			nameInput = driver.findElement(By.name("name"));
		}
		nameInput.sendKeys(name);
		nameInput.submit();
	}
	
	public String getTestName(long id) {
		goToListPage();
		try {
			WebElement testNameElt = driver.findElement(By.id(editTaskPrefix + id));
			return testNameElt.getText();
		} catch (NoSuchElementException x) {
			return null;
		}
	}
	
	@Test
	public void loginLogout() {
		this.loginIfNecessary(testUser);
		assertTrue(this.logoutIfPossible());
	}
	
	@Test
	public void emptyTestAtStartup() {
		assertEquals(0, getTests().size());
	}
	
	@Test
	public void createTask() {
		String name = "A task name";
		long id = createTestAndGetId(name);
		assertEquals(name, getTestName(id));
	}
	
	@Test
	public void createTasks() {
		Set<String> names = new TreeSet<>();
		names.add("test name 1");
		names.add("test name 2");
		names.add("test name 3");
		names.add("test name 4");
		
		for (String name : names) {
			createTest(name);
		}
		
		Set<String> testNames = new TreeSet<>();
		testNames.addAll(getTestNames());
		
		assertEquals(names, testNames);
	}
	
	@Test
	public void createTasksForDifferentUsers() {
		String otherUser = "Other User";
		loginIfNecessary(otherUser);
		Set<String> names = new TreeSet<>();
		names.add("o test name 1");
		names.add("o test name 2");
		names.add("o test name 3");
		names.add("o test name 4");
		
		for (String name : names) {
			createTest(name);
		}
		
		try {
			loginIfNecessary(testUser);
			assertTrue(getTestNames().isEmpty());
			
			loginIfNecessary(otherUser);
			
			Set<String> testNames = new TreeSet<>();
			testNames.addAll(getTestNames());
			
			assertEquals(names, testNames);
		} finally {
			deleteAllTestsStatic();
		}
	}
	
	@Test
	public void deleteOneTaskAmongMany() {
		String delSuffix = " - del";
		Set<String> names = new TreeSet<>();
		names.add("test name 1");
		names.add("test name 2"+delSuffix);
		names.add("test name 3");
		names.add("test name 4");
		
		Long delId = null;
		Iterator<String> namesIt = names.iterator();
		while (namesIt.hasNext()) {
			String name = namesIt.next();
			if (name.endsWith(delSuffix) && delId == null) {
				delId = createTestAndGetId(name);
				namesIt.remove();
			} else {
				createTest(name);
			}
		}
		
		if (delId == null) throw new NullPointerException("A test name should be suffixed by '" + delSuffix + '\'');
		deleteTest(delId);
		
		Set<String> testNames = new TreeSet<>();
		testNames.addAll(getTestNames());
		
		assertEquals(names, testNames);
	}

}
