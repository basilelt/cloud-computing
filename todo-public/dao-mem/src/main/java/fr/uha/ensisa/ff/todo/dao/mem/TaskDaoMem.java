package fr.uha.ensisa.ff.todo.dao.mem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.uha.ensisa.ff.todo.Task;
import fr.uha.ensisa.ff.todo.dao.TaskDao;

public class TaskDaoMem implements TaskDao {

	private final Map<String /* user */, Map<Long, byte[]>> store = new ConcurrentSkipListMap<>();
	private final Kryo kryo;
	
	public TaskDaoMem() {
		this.kryo = new Kryo();
		kryo.register(Task.class);
	}
	
	protected byte[] toBytes(Task task) {
		try (
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				Output out = new Output(buf);) {
			kryo.writeObject(out, task);
			out.close();
			return buf.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Task fromBytes(byte [] bytes) {
		if (bytes == null) return null;
		try (Input in = new Input(bytes)) {
			return kryo.readObject(in, Task.class);
		}
	}

	public void clear() {
		store.clear();
	}

	@Override
	public void clear(String user) {
		store.remove(user == null ? "" : user);
	}

	@Override
	public void store(final Task task) {
		Map<Long, byte[]> s = store.computeIfAbsent(task.getUser() == null ? "" : task.getUser(), (user) -> new ConcurrentHashMap<>());
		long id = s.size();
		task.setId(id);
		while (s.computeIfAbsent(id, i -> {
			task.setId(i);
			return toBytes(task);
		}) == null) {
			id++;
		}
	}

	@Override
	public void update(final Task task) {
		if (task == null) return;
		Map<Long, byte[]> s = store.get(task.getUser() == null ? "" : task.getUser());
		if (s == null) return;
		s.computeIfPresent(task.getId(), (id, old) -> toBytes(task));
	}

	@Override
	public void remove(Task task) {
		if (task == null) return;
		Map<Long, byte[]> s = store.get(task.getUser() == null ? "" : task.getUser());
		if (s != null) {
			s.remove(task.getId());
			if (s.isEmpty()) store.computeIfPresent(task.getUser() == null ? "" : task.getUser(), (user, ss) -> ss.isEmpty() ? null : ss);
		}
	}

	@Override
	public Task find(String user, long id) {
		Map<Long, byte[]> s = store.get(user == null ? "" : user);
		return s == null ? null : fromBytes(s.get(id));
	}

	@Override
	public Collection<Task> findAll(String user) {
		Map<Long, byte[]> s = store.get(user == null ? "" : user);
		return s == null ? Collections.emptyList() : s.values().stream().map(bytes -> fromBytes(bytes)).collect(Collectors.toList());
	}

	@Override
	public long count(String user) {
		Map<Long, byte[]> s = store.get(user == null ? "" : user);
		return s == null ? 0 : s.size();
	}

}
