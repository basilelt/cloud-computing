package fr.uha.ensisa.ff.todo.app.config;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

public final class Session implements Serializable {
	private String id;
	
	private String name;
	
	private long endTime;
	
	public Session(String name) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.touch();
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return this.name;
	}
	
	public boolean isValid() {
		return this.endTime > System.currentTimeMillis();
	}

	public void touch() {
		this.endTime = System.currentTimeMillis() + SessionInterceptor.SessionTimeout;
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		int version = ois.readUnsignedByte();
		if (version > 1) throw new UnsupportedOperationException("Cannot deserialize " + Session.class.getName() + " with stream of version ");
		this.id = DataInputStream.readUTF(ois);
		this.endTime = ois.readLong();
		this.name = DataInputStream.readUTF(ois);
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.write(0);
		oos.writeUTF(this.id);
		oos.writeLong(this.endTime);
		oos.writeUTF(this.name);
	}
}
