package fr.uha.ensisa.ff.todo.app;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

import fr.uha.ensisa.ff.todo.Task;
import fr.uha.ensisa.ff.todo.app.controller.TaskController;
import fr.uha.ensisa.ff.todo.dao.DaoFactory;
import fr.uha.ensisa.ff.todo.dao.TaskDao;

public class TaskControllerTest {
	
	private static final String USER = "user1";

	@Mock public DaoFactory daoFactory;
	@Mock public TaskDao daoTask;
	
	public TaskController sut;
	
	@Before
	public void prepareDao() {
		MockitoAnnotations.initMocks(this); // crée les @Mock
		when(daoFactory.getTaskDao()).thenReturn(this.daoTask);
		sut = new TaskController(); // System Under Test
		sut.daoFactory = this.daoFactory;
	}

	@Test
	public void emptyList() throws IOException {
		ModelAndView ret = sut.list(USER);
		Collection<Task> tasks = (Collection<Task>)ret.getModelMap().get("tasks");
		assertNotNull(tasks);
		assertTrue(tasks.isEmpty());
	}
	
	@Test
	public void createTask() throws IOException {
		sut.create(USER); // Il faut qu'un persist aie été appelé sur daoTask
		verify(daoTask).store(any(Task.class));
	}
	
	@Test
	public void resistWeirdId() throws IOException {
		when(daoTask.count(USER)).thenReturn(1l);
		Task t2 = mock(Task.class);
		when(t2.getId()).thenReturn(2l);
		when(daoTask.find(USER, 2)).thenReturn(t2);
		sut.create(USER);
		ArgumentCaptor<Task> persisted = ArgumentCaptor.forClass(Task.class);
		verify(daoTask).store(persisted.capture());
		assertNotEquals(1l, persisted.getValue().getId());
	}
	
	@Test
	public void delete() throws IOException {
		long deletedId = 5l;
		sut.delete(USER, deletedId);
		ArgumentCaptor<Task> remove = ArgumentCaptor.forClass(Task.class);
		verify(daoTask).remove(remove.capture());
		assertEquals(deletedId, remove.getValue().getId());
	}
}
