package one.slope.slip.asset.container.rt3;

import java.net.URI;
import java.net.URISyntaxException;

public class RT3Descriptor {
	private final int index;
	private final int start;
	private final int size;
	private final int id;
	
	public RT3Descriptor(int index, int id, int size, int start) {
		this.index = index;
		this.id = id;
		this.size = size;
		this.start = start;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof RT3Descriptor) {
			RT3Descriptor o = (RT3Descriptor)other;
			return o.id == id && o.size == size && o.start == start && o.index == index;
		}
		
		return super.equals(other);
	}
	
	public URI uri() throws URISyntaxException {
		return new URI("/" + index + "/" + id);
	}
	
	public int id() {
		return id;
	}
	
	public int index() {
		return index;
	}
	
	public int start() {
		return start;
	}
	
	public int size() {
		return size;
	}
}
