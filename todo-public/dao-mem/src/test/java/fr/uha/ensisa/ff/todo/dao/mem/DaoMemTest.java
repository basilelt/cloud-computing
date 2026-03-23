package fr.uha.ensisa.ff.todo.dao.mem;

import org.junit.Before;

import fr.uha.ensisa.ff.todo.GenericTest;
import fr.uha.ensisa.ff.todo.dao.DaoFactory;

public class DaoMemTest extends GenericTest {
	
	public DaoMemTest(String user) {
		super(user);
	}

	private DaoFactoryMem sut;
	
	@Before
	public void createAndSetupSut() {
		this.sut = new DaoFactoryMem();
	}

	@Override
	public DaoFactory getSut() {
		return this.sut;
	}

}
