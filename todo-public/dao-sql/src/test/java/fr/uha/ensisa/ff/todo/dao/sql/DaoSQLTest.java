package fr.uha.ensisa.ff.todo.dao.sql;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import fr.uha.ensisa.ff.todo.GenericTest;
import fr.uha.ensisa.ff.todo.dao.DaoFactory;

public class DaoSQLTest extends GenericTest {
	public DaoSQLTest(String user) {
		super(user);
	}

	private static DataSource ds;
	
	@BeforeClass
	public static void createDatabaseAndConnection() throws PropertyVetoException {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		ds.setDriverClass( "org.hsqldb.jdbc.JDBCDriver" );
		ds.setJdbcUrl("jdbc:hsqldb:mem:memdb;shutdown=true");
		ds.setUser("SA");
		ds.setPassword("");
		DaoSQLTest.ds = ds;
	}
	
	@AfterClass
	public static void shutdownDatabase() throws SQLException {
		try (Connection conn = ds.getConnection()) {
			Statement st = conn.createStatement();
			st.execute("SHUTDOWN");
			conn.close();
		}
	}
	
	public DaoFactorySQL sut;
	
	@Before
	public void clearDatabaseAndSetupSUT() throws SQLException {
		try (Connection conn = ds.getConnection()) {
			conn.createStatement().executeUpdate("TRUNCATE SCHEMA public AND COMMIT");
			sut = new DaoFactorySQL(ds, DaoFactorySQL.Database.HSQLDB);
		}
	}

	@Override
	public DaoFactory getSut() {
		return this.sut;
	}

}
