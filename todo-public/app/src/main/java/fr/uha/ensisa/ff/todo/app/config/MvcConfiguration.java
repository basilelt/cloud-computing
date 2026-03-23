package fr.uha.ensisa.ff.todo.app.config;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import fr.uha.ensisa.ff.todo.dao.DaoFactory;
import fr.uha.ensisa.ff.todo.dao.mem.DaoFactoryMem;
import fr.uha.ensisa.ff.todo.dao.sql.DaoFactorySQL;

@Configuration
@ComponentScan(basePackages = "fr.uha.ensisa.ff.todo.app")
@EnableWebMvc
public class MvcConfiguration implements WebMvcConfigurer {

	@Bean
	public ViewResolver getViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Bean
	public String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "ERRONEOUS HOSTNAME: " + e.getMessage();
		}
	}

	@Bean
	public Boolean isAuthEnabled() {
		String res = getEnv("AUTH");
		if (res == null)
			return true;
		res = res.toLowerCase();
		return !Arrays.asList("0", "false", "no", "off").contains(res);
	}
	
	@Bean(destroyMethod = "shutdown")
	public HazelcastInstance getHazelcastInstance() {
		int ttlSeconds = SessionInterceptor.SessionTimeoutS + 5;

		NearCacheConfig nearCacheConfig = new NearCacheConfig("todo-sessions")
				.setTimeToLiveSeconds(ttlSeconds)
				.setMaxIdleSeconds(SessionInterceptor.SessionTimeoutS)
				.setInvalidateOnChange(true);

		MapConfig mapConfig = new MapConfig("todo-sessions")
				.setBackupCount(0)
				.setAsyncBackupCount(1)
				.setTimeToLiveSeconds(ttlSeconds)
				.setNearCacheConfig(nearCacheConfig);

		Config config = new Config();
		config.setInstanceName("todo-hazelcast");
		config.addMapConfig(mapConfig);

		// Detect Kubernetes by reading the namespace from the service account file.
		// If present, use DNS-based discovery via a headless service (no K8s API needed).
		// Outside Kubernetes, fall back to default multicast discovery.
		try {
			String namespace = new String(Files.readAllBytes(
					Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace"))).trim();
			String serviceName = getEnv("HAZELCAST_SERVICE_NAME");
			if (serviceName != null && !serviceName.isEmpty()) {
				String serviceDns = serviceName + "." + namespace + ".svc.cluster.local";
				config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
				config.getNetworkConfig().getJoin().getKubernetesConfig()
						.setEnabled(true)
						.setProperty("service-dns", serviceDns);
			}
		} catch (IOException e) {
			// Not running in Kubernetes — use default multicast discovery
		}

		return Hazelcast.newHazelcastInstance(config);
	}

	@Bean
	public Map<String /* session id */, Session /* user */> getSessions() {
		return getHazelcastInstance().getMap("todo-sessions");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
	}

	@Bean
	public SessionInterceptor getSessionInterceptor() {
		return new SessionInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (isAuthEnabled())
			registry.addInterceptor(getSessionInterceptor());
	}

	public static String getEnv(String... envVars) {
		return getEnvDefault(null, (String[]) envVars);
	}

	public static String getEnvDefault(String defaultValue, String... envVars) {
		String ret;
		for (String env : envVars) {
			ret = System.getenv(env);
			if (ret != null)
				return ret.trim();
			ret = System.getProperty(env);
			if (ret != null)
				return ret.trim();
		}
		return defaultValue;
	}

	@Bean
	public DaoFactory getDaoFactory() {
		// Trying hard to get a DB

		// If DATABASE_URL is set, then we have already the connection URL (as in
		// Heroku)
		String dbUrlStr = getEnv("DATABASE_URL", "CLEARDB_DATABASE_URL");
		if (dbUrlStr != null && dbUrlStr.length() > 0) {
			ComboPooledDataSource cpds = null;
			try {
				URI dbUrl = new URI(dbUrlStr); // NO _ or - in the name !!!
				Class.forName("com.mysql.cj.jdbc.Driver");
				String dbType = dbUrl.getScheme();
				if ("postgres".equalsIgnoreCase(dbType))
					dbType = "postgresql";
				String dbName = dbUrl.getPath();
				String[] userPass = dbUrl.getUserInfo().split(":");
				String userName = userPass[0];
				String password = userPass.length > 1 ? userPass[1] : "";
				String hostname = dbUrl.getHost();
				String port = dbUrl.getPort() >= 0 ? Integer.toString(dbUrl.getPort())
						: ("postgresql".equals(dbType) ? "5432" : "3306");
				String options = dbUrl.getRawQuery();
				String jdbcUrl = "jdbc:" + dbType + "://" + hostname + ":" + port + dbName;
				if (options != null && (options = options.trim()).length() > 0)
					jdbcUrl += "?" + options;
				System.out.println(
						"Getting remote connection with connection string from DATABASE_URL environment variable.");
				cpds = new ComboPooledDataSource();
				cpds.setDriverClass("org.postgresql.Driver"); // loads the jdbc driver
				cpds.setJdbcUrl(jdbcUrl);
				if (userName.length() != 0)
					cpds.setUser(userName);
				if (password.length() != 0)
					cpds.setPassword(password);
				DaoFactorySQL ret = new DaoFactorySQL(cpds, dbType);
				System.out.println("Remote connection to " + dbType + " successful.");
				return ret;
			} catch (ClassNotFoundException e) {
				System.err.println(e.toString());
			} catch (URISyntaxException e) {
				System.out.println("Invalid DATABASE_URL environment variable: " + e.getMessage());
			} catch (PropertyVetoException e) {
				System.out.println("Problem while getting data source: " + e.getMessage());
			} catch (SQLException e) {
				cpds.close();
				System.out.println("Failed to connect to " + dbUrlStr + ": " + e.getMessage());
			} catch (Exception e) {
				System.err.println(e.toString());
				System.out.println("Failed to connect to " + dbUrlStr + ": " + e.getMessage());
			}
		}

		// Trying to connect to AWS RDS or local MySQL or PostgreSQL
		String dbName = getEnvDefault("test", "RDS_DB_NAME");
		if (null != dbName && dbName.trim().length() > 0) {
			for (String[] type_port : new String[][] { new String[] { "mysql", "3306" },
					new String[] { "postgresql", "5432" } }) {
				ComboPooledDataSource cpds = null;
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					String userName = getEnvDefault("root", "RDS_USERNAME");
					String password = getEnvDefault("", "RDS_PASSWORD");
					String hostname = getEnvDefault("localhost", "RDS_HOSTNAME");
					String port = getEnvDefault(type_port[1], "RDS_PORT");
					String options = getEnvDefault("", "RDS_OPTIONS");

					// Testing for open port
					try {
						Socket socket = new Socket();
						socket.connect(new InetSocketAddress(hostname, Integer.parseInt(port)), 2500);
						socket.close();
					} catch (NumberFormatException e) {
						System.err.println("Illegal port " + port + " for DB connection to " + hostname);
						continue;
					} catch (IOException e) {
						System.out.println("Giving up connecting " + type_port[0] + " on " + hostname + ':' + port
								+ ": port is not reachable");
						continue;
					}

					String jdbcUrl = "jdbc:" + type_port[0] + "://" + hostname + ":" + port + "/" + dbName;
					if (options.length() > 0)
						jdbcUrl += '?' + options;
					System.out.println(
							"Getting remote connection with connection string from RDS_* environment variables. "
									+ jdbcUrl);
					cpds = new ComboPooledDataSource();
					cpds.setLoginTimeout(5);
					cpds.setJdbcUrl(jdbcUrl);
					if (userName.length() != 0)
						cpds.setUser(userName);
					if (password.length() != 0)
						cpds.setPassword(password);
					DaoFactorySQL ret = new DaoFactorySQL(cpds, type_port[0]);
					System.out.println("Remote connection to " + type_port[0] + " successful.");
					return ret;
				} catch (ClassNotFoundException e) {
					System.err.println(e.toString());
				} catch (SQLException e) {
					cpds.close();
					System.out.println("Failed to connect to " + type_port[0] + ": " + e.getMessage());
				}
			}
		}

		System.out.println("Using in-memory store");
		return new DaoFactoryMem();
	}

	@PreDestroy
	public void destroying() throws SQLException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				// This driver was registered by the webapp's ClassLoader, so deregister it:
				try {
					System.out.println("Deregistering JDBC driver " + driver);
					DriverManager.deregisterDriver(driver);
				} catch (SQLException ex) {
					System.err.println("Error deregistering JDBC driver " + driver);
					ex.printStackTrace();
				}
			} else {
				// driver was not registered by the webapp's ClassLoader and may be in use
				// elsewhere
				System.out.println("Not deregistering JDBC driver " + driver
						+ " as it does not belong to this webapp's ClassLoader");
			}

		}
	}

}
