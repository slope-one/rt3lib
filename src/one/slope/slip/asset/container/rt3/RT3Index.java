package one.slope.slip.asset.container.rt3;

import java.util.HashMap;
import java.util.Map;

import one.slope.slip.io.DataType;
import one.slope.slip.io.SuperBuffer;

public class RT3Index {
	public static final int DATA_SIZE = 6;
	
	private final Map<Integer, RT3Descriptor> index = new HashMap<Integer, RT3Descriptor>();
	private final int id;
	
	public RT3Index(int id) {
		this.id = id;
	}
	
	public boolean load(SuperBuffer buffer) {
		while (buffer.hasRemaining()) {
			int file = (int)buffer.position() / DATA_SIZE;
			index.put(file, new RT3Descriptor(id, file, buffer.get(DataType.TRIPLE), buffer.get(DataType.TRIPLE)));
		}
		
		return true;
	}
	
	public RT3Descriptor get(int id) {
		return index.get(id);
	}
	
	public int size() {
		return index.size();
	}
	
	public int id() {
		return id;
	}
}
