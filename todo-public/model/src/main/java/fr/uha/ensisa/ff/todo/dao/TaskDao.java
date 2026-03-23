package fr.uha.ensisa.ff.todo.dao;

import java.util.Collection;

import fr.uha.ensisa.ff.todo.Task;

public interface TaskDao {

	public void store(Task task);
	public void update(Task task);
	public void remove(Task task);
	public Task find(String user, long id);
	public Collection<Task> findAll(String user);
	public long count(String user);
	void clear(String user);
}
