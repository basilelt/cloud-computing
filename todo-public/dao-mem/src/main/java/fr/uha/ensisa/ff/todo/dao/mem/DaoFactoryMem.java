package fr.uha.ensisa.ff.todo.dao.mem;

import fr.uha.ensisa.ff.todo.dao.DaoFactory;
import fr.uha.ensisa.ff.todo.dao.TaskDao;

public class DaoFactoryMem implements DaoFactory {

	public final TaskDao taskDao = new TaskDaoMem();
	public TaskDao getTaskDao() {
		return this.taskDao;
	}
	
	public String toString() {
		return "In-Memory DAO " + this.hashCode();
	}

}
