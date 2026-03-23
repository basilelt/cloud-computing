package fr.uha.ensisa.ff.todo;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Before;
import org.junit.Test;

import fr.uha.ensisa.ff.todo.Task;

public class TaskTest {
	
	Task sut;
	
	@Before
	public void createTask() {
		sut = new Task();
	}

	@Test
	public void setId() {
		long id = -58922808l;
		sut.setId(id);
		assertEquals(id, sut.getId());
	}

	@Test
	public void setName() {
		assertNull(sut.getName());
		String name = "Make more tests";
		sut.setName(name);
		assertEquals(name, sut.getName());
	}

	@Test
	public void setNameMatchers() {
		assertThat(sut.getName(), is(nullValue()));
		String name = "Make more tests";
		sut.setName(name);
		assertThat(sut.getName(), is(name));
	}

}
