package fr.uha.ensisa.ff.todo.dao.sql;

import java.sql.SQLException;
import java.util.Locale;

import javax.sql.DataSource;

import fr.uha.ensisa.ff.todo.dao.DaoFactory;
import fr.uha.ensisa.ff.todo.dao.TaskDao;

public class DaoFactorySQL implements DaoFactory {
	
	public static enum Database { MYSQL, POSTGRES, HSQLDB;
		public static Database getFromType(String type) {
			type = type.toLowerCase(Locale.getDefault());
			if (type.contains("mysql")) return MYSQL;
			if (type.contains("postgres")) return POSTGRES;
			if (type.contains("hsqldb")) return HSQLDB;
			return null;
		}
	}
	
	public final Database db;
	public final TaskDao taskDao;

	public DaoFactorySQL(DataSource ds, Database db) throws SQLException {
		super();
		this.db = db;
		this.taskDao = new TaskDaoSQL(ds, db);
	}
	
	public DaoFactorySQL(DataSource ds, String dbType) throws SQLException {
		this(ds, Database.getFromType(dbType));
	}

	public TaskDao getTaskDao() {
		return this.taskDao;
	}
	
	public String toString() {
		return this.db.name() + " DAO " + this.hashCode();
	}

}
