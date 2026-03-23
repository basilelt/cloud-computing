package fr.uha.ensisa.ff.todo;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import fr.uha.ensisa.ff.todo.dao.DaoFactory;

@RunWith(Parameterized.class)
public abstract class GenericTest {
	@Parameters()
	public static Iterable<Object[]> users() {
		return Arrays.asList(new Object[][] { { "user1" }, { null }, { "" } });
	}
	
	private final String user;

	public abstract DaoFactory getSut();

	public GenericTest(String user) {
		this.user = user;
	}

	private Task createAnStoreTask(String user, String name) {
		if (name == null) name = "a test task name";
		
		Task t = new Task();
		t.setName(name);
		t.setUser(user);

		this.getSut().getTaskDao().store(t);
		
		return t;
	}

	private Set<Task> createAndStoreTasks(String user, String namePrefix, int number) {
		if (namePrefix == null) namePrefix = "task ";
		Set<Task> ret = new HashSet<>();
		for(int i = 1; i <= number; i++) {
			String name = namePrefix + i;
			ret.add(this.createAnStoreTask(user, name));
		}
		return ret;
	}
	
	@Test
	public void countNoTask() {
		assertEquals(0, this.getSut().getTaskDao().count(user));
	}

	@Test
	public void allNoTask() {
		assertThat(this.getSut().getTaskDao().findAll(user), emptyCollectionOf(Task.class));
	}

	@Test
	public void getNoTask() {
		assertNull(this.getSut().getTaskDao().find(user, 10));
	}

	@Test
	public void insertTask() {
		String name = "a test task name for insert";
		Task t = createAnStoreTask(user, name);
		
		assertThat(t.getId(), greaterThan(-1l));
		
		Task found = this.getSut().getTaskDao().find(user, t.getId());
		
		assertNotNull(found);
		assertEquals(name, found.getName());
		assertEquals(user, found.getUser());

		assertEquals(0, this.getSut().getTaskDao().count("user2"));
		assertThat(this.getSut().getTaskDao().findAll("user2"), emptyCollectionOf(Task.class));
		assertNull(this.getSut().getTaskDao().find("user2", t.getId()));
	}

	@Test
	public void getInexistentTask() {
		Task t = createAnStoreTask(user, null);
		
		Task found = this.getSut().getTaskDao().find(user, t.getId() + 10);
		
		assertNull(found);
	}

	@Test
	public void allOneTask() {
		Task t = createAnStoreTask(user, null);
		
		Collection<Task> res = this.getSut().getTaskDao().findAll(user);
		List<String> ret = res.stream().map(Task::getName).collect(Collectors.toList());
		assertEquals(Arrays.asList(t.getName()), ret);
	}

	@Test
	public void allManyTask() {
		Set<Task> tasks = createAndStoreTasks(user, null, 10);
		Set<String> expected = tasks.stream().map(Task::getName).collect(Collectors.toSet());
		
		Collection<Task> res = this.getSut().getTaskDao().findAll(user);
		Set<String> ret = res.stream().map(Task::getName).collect(Collectors.toSet());
		assertEquals(expected, ret);
	}

	@Test
	public void countManyTask() {
		int number = 10;
		createAndStoreTasks(user, null, number);
		
		assertEquals(number, this.getSut().getTaskDao().count(user));
	}

	@Test
	public void deleteManyTask() {
		createAndStoreTasks(user, null, 10);
		
		this.getSut().getTaskDao().clear(user);
		
		assertEquals(0, this.getSut().getTaskDao().count(user));
	}

	@Test
	public void reusingDeletedTaskTable() {
		createAndStoreTasks(user, null, 3);
		this.getSut().getTaskDao().clear(user);
		int number = 4;
		Collection<Task> tasks = createAndStoreTasks(user, null, number);
		Set<String> expected = tasks.stream().map(Task::getName).collect(Collectors.toSet());
		
		assertEquals(number, this.getSut().getTaskDao().count(user));
		
		Collection<Task> res = this.getSut().getTaskDao().findAll(user);
		Set<String> ret = res.stream().map(Task::getName).collect(Collectors.toSet());
		assertEquals(expected, ret);
	}
	
	@Test
	public void deleteANonTask() {
		Collection<Task> tasks = createAndStoreTasks(user, null, 3);
		Set<Long> ids = tasks.stream().map(Task::getId).collect(Collectors.toSet());
		
		long idToDelete = ids.size() + 1;
		while (ids.contains(idToDelete)) idToDelete++;
		
		Task toDelete = new Task();
		toDelete.setId(idToDelete);
		toDelete.setUser(user);
		this.getSut().getTaskDao().remove(toDelete);
		
		assertNull(this.getSut().getTaskDao().find(user, idToDelete));
		assertEquals(ids, this.getSut().getTaskDao().findAll(user).stream().map(Task::getId).collect(Collectors.toSet()));
	}
	
	@Test
	public void deleteATask() {
		Collection<Task> tasks = createAndStoreTasks(user, null, 3);
		Set<Long> ids = tasks.stream().map(Task::getId).collect(Collectors.toSet());
		
		long idToDelete = ids.iterator().next();
		
		Task toDelete = new Task();
		toDelete.setId(idToDelete);
		toDelete.setUser(user);
		this.getSut().getTaskDao().remove(toDelete);
		
		assertNull(this.getSut().getTaskDao().find(user, idToDelete));
		ids.remove(idToDelete);
		assertEquals(ids, this.getSut().getTaskDao().findAll(user).stream().map(Task::getId).collect(Collectors.toSet()));
	}
	
	@Test
	public void deleteAnotherTask() {
		Collection<Task> tasks = createAndStoreTasks(user, null, 3);
		Set<Long> ids = tasks.stream().map(Task::getId).collect(Collectors.toSet());
		
		Iterator<Long> it = ids.iterator();
		it.next();
		long idToDelete = it.next();
		
		Task toDelete = new Task();
		toDelete.setId(idToDelete);
		toDelete.setUser(user);
		this.getSut().getTaskDao().remove(toDelete);
		
		assertNull(this.getSut().getTaskDao().find(user, idToDelete));
		ids.remove(idToDelete);
		assertEquals(ids, this.getSut().getTaskDao().findAll(user).stream().map(Task::getId).collect(Collectors.toSet()));
	}
	
	@Test
	public void updateANonTask() {
		Collection<Task> tasks = createAndStoreTasks(user, null, 3);
		Set<Long> ids = tasks.stream().map(Task::getId).collect(Collectors.toSet());
		Set<String> names = tasks.stream().map(Task::getName).collect(Collectors.toSet());
		
		long idToUpdate = ids.size() + 1;
		while (ids.contains(idToUpdate)) idToUpdate++;
		
		Task taskToUpdate = new Task();
		taskToUpdate.setId(idToUpdate);
		taskToUpdate.setUser(user);
		String newName = "the new name for the dummy task";
		taskToUpdate.setName(newName);
		
		this.getSut().getTaskDao().update(taskToUpdate);
		
		// No tasks should not be updated
		assertThat(names, not(hasItem(newName))); // To enfore that test is well written
		assertEquals(names, this.getSut().getTaskDao().findAll(user).stream().map(Task::getName).collect(Collectors.toSet()));
	}
	
	@Test
	public void updateATask() {
		Collection<Task> tasks = createAndStoreTasks(user, null, 3);
		
		Iterator<Task> it = tasks.iterator();
		Task taskToUpdate = it.next();
		String newName = "the new name for the task";
		taskToUpdate.setName(newName);
		taskToUpdate.setUser(user);
		
		// Nothing happens before update
		assertNotEquals(newName, this.getSut().getTaskDao().find(user, taskToUpdate.getId()).getName());
		
		this.getSut().getTaskDao().update(taskToUpdate);
		
		assertEquals(newName, this.getSut().getTaskDao().find(user, taskToUpdate.getId()).getName());
		
		// Other tasks should not be updated
		Set<String> names = tasks.stream().map(Task::getName).collect(Collectors.toSet());
		assertThat(names, hasItem(newName)); // To enfore that test is well written
		assertEquals(names, this.getSut().getTaskDao().findAll(user).stream().map(Task::getName).collect(Collectors.toSet()));
	}
	
	@Test
	public void updateAnotherTask() {
		Collection<Task> tasks = createAndStoreTasks(user, null, 3);
		
		Iterator<Task> it = tasks.iterator();
		it.next();
		Task taskToUpdate = it.next();
		String newName = "the new name for the task";
		taskToUpdate.setName(newName);
		
		// Nothing happens before update
		assertNotEquals(newName, this.getSut().getTaskDao().find(user, taskToUpdate.getId()).getName());
		
		this.getSut().getTaskDao().update(taskToUpdate);
		
		assertEquals(newName, this.getSut().getTaskDao().find(user, taskToUpdate.getId()).getName());
		
		// Other tasks should not be updated
		Set<String> names = tasks.stream().map(Task::getName).collect(Collectors.toSet());
		assertThat(names, hasItem(newName)); // To enfore that test is well written
		assertEquals(names, this.getSut().getTaskDao().findAll(user).stream().map(Task::getName).collect(Collectors.toSet()));
	}

}
