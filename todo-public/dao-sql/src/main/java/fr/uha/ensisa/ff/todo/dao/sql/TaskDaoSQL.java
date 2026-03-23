package fr.uha.ensisa.ff.todo.dao.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

import javax.sql.DataSource;

import fr.uha.ensisa.ff.todo.Task;
import fr.uha.ensisa.ff.todo.dao.TaskDao;
import fr.uha.ensisa.ff.todo.dao.sql.DaoFactorySQL.Database;

public class TaskDaoSQL implements TaskDao {
	public static final String TABLE = "todo";
	
	private final DataSource ds;
	
	public TaskDaoSQL(DataSource ds, Database db) throws SQLException {
		this.ds = ds;
		
		
		try (Connection conn = ds.getConnection(); Statement st =  conn.createStatement()) {
			switch (db) {
			case MYSQL:
				st.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE + " (id INTEGER NOT NULL AUTO_INCREMENT, owner VARCHAR(100), name VARCHAR(100), PRIMARY KEY (id), INDEX(owner))");
				break;
			case POSTGRES:
				st.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE + " (id SERIAL PRIMARY KEY, owner VARCHAR(100), name VARCHAR(100))");
				st.executeUpdate("CREATE INDEX IF NOT EXISTS owner_index ON " + TABLE + " (owner)");
				break;
			case HSQLDB:
				st.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE + " (id INTEGER IDENTITY PRIMARY KEY, owner VARCHAR(100), name VARCHAR(100))");
				st.executeUpdate("CREATE INDEX IF NOT EXISTS owner_index ON " + TABLE + " (owner)");
				break;

			default:
				throw new RuntimeException("Unknown database type: " + db);
			}
		}
	}

	public void clear() {
		try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {
			st.execute("TRUNCATE TABLE " + TABLE);
		} catch (SQLException e) {
			throw new RuntimeException("Cannot clear tasks: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void clear(String user) {
		if (user == null) user = "";
		try (Connection conn = ds.getConnection(); PreparedStatement st = conn.prepareStatement("DELETE FROM " + TABLE + " WHERE " + TABLE + ".owner = ?")) {
			st.setString(1, user);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot delete tasks for user " + user + ": " + e.getMessage(), e);
		}
	}

	@Override
	public void store(Task task) {
		if (task.getUser() == null) task.setUser("");
		try (Connection conn = ds.getConnection(); PreparedStatement st = conn.prepareStatement("INSERT INTO " + TABLE + " (owner, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
			st.setString(1, task.getUser());
			st.setString(2, task.getName());
			st.executeUpdate();
			
			try (ResultSet res = st.getGeneratedKeys()) {
				boolean hasNext = res.next();
				assert hasNext;
				long id = res.getLong(1);
				task.setId(id);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Cannot store task " + task.getName() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public void update(Task task) {
		if (task.getUser() == null) task.setUser("");
		try (Connection conn = ds.getConnection(); PreparedStatement st = conn.prepareStatement("UPDATE " + TABLE + " SET name = ? WHERE " + TABLE + ".id = ? AND " + TABLE + ".owner = ?")) {
			st.setLong(2, task.getId());
			st.setString(3, task.getUser());
			st.setString(1, task.getName());
			st.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot insert task " + task.getName() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public void remove(Task task) {
		if (task.getUser() == null) task.setUser("");
		try (Connection conn = ds.getConnection(); PreparedStatement st = conn.prepareStatement("DELETE FROM " + TABLE + " WHERE " + TABLE + ".id = ? AND " + TABLE + ".owner = ?")) {
			st.setLong(1, task.getId());
			st.setString(2, task.getUser());
			st.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot delete task " + task.getName() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public Task find(String user, long id) {
		final String _user = user;
		if (user == null) user = "";
		try (Connection conn = ds.getConnection(); PreparedStatement st = conn.prepareStatement("SELECT name FROM " + TABLE + " WHERE " + TABLE + ".id = ? AND " + TABLE + ".owner = ?")) {
			st.setLong(1, id);
			st.setString(2, user);
			
			try (ResultSet res = st.executeQuery()) {
			
				if (! res.next()) return null;
				
				Task ret = new Task();
				ret.setId(id);
				ret.setName(res.getString(1));
				ret.setUser(_user);
				
				assert !res.next();
				
				return ret;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Cannot find task with id " + id + ": " + e.getMessage(), e);
		}
	}

	@Override
	public Collection<Task> findAll(String user) {
		if (user == null) user = "";
		try (Connection conn = ds.getConnection(); PreparedStatement st = conn.prepareStatement("SELECT id, name FROM " + TABLE + " WHERE " + TABLE + ".owner = ?")) {
			st.setString(1, user);
			try (ResultSet res = st.executeQuery()) {
			
				Collection<Task> ret = new LinkedList<Task>();
				
				while (res.next()) {
					Task t = new Task();
					t.setId(res.getLong(1));
					t.setUser(user);
					t.setName(res.getString(2));
					ret.add(t);
				}
				
				return ret;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Cannot find tasks list: " + e.getMessage(), e);
		}
	}

	@Override
	public long count(String user) {
		if (user == null) user = "";
		try (Connection conn = ds.getConnection(); PreparedStatement st = conn.prepareStatement("SELECT COUNT(id) FROM " + TABLE + " WHERE " + TABLE + ".owner = ?")) {
			st.setString(1, user);
			try (ResultSet res = st.executeQuery()) {
			
				boolean hasNext = res.next();
				assert hasNext;
				
				return res.getLong(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Cannot count tasks: " + e.getMessage(), e);
		}
	}

}
