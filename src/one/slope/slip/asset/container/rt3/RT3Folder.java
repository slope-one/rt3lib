package one.slope.slip.asset.container.rt3;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import one.slope.slip.asset.StoreFile;
import one.slope.slip.asset.StoreFolder;
import one.slope.slip.io.DataType;
import one.slope.slip.io.SuperBuffer;

public class RT3Folder implements StoreFolder {
	public static final int FILE_HEADER_SIZE = 10;
	public static final int DATA_HEADER_SIZE = 6;
	
	private final Map<Integer, StoreFile> entries = new HashMap<>();
	private final StoreFile parent;
	private final SuperBuffer compressed;
	private SuperBuffer decompressed;
	
	public RT3Folder(StoreFile parent, SuperBuffer buffer) {
		this.parent = parent;
		this.compressed = buffer;
	}

	@Override
	public SuperBuffer data() throws IOException {
		if (decompressed == null) {
			//byte[] array = this.compressed.array();
			//this.compressed.reset();
			int decompressedSize = compressed.getUnsigned(DataType.TRIPLE);
			int compressedSize = compressed.getUnsigned(DataType.TRIPLE);
			
			if (decompressedSize != compressedSize) {
				byte[] compressedData = new byte[compressedSize + 4];
				byte[] decompressedData = new byte[decompressedSize];
				byte[] comp = new byte[compressedSize];
				compressed.get(comp);
				System.arraycopy(comp, 0, compressedData, 4, comp.length);
				
				// set up bzip header, since it is always excluded
				compressedData[0] = 'B';
				compressedData[1] = 'Z';
				compressedData[2] = 'h';
				compressedData[3] = '1';
				
				try (DataInputStream is = new DataInputStream(new BZip2CompressorInputStream(new ByteArrayInputStream(compressedData)))) {
					is.readFully(decompressedData);
					this.decompressed = new SuperBuffer(ByteBuffer.wrap(decompressedData));
				}
			}
			else {
				this.decompressed = compressed;
			}
			
			int entryCount = this.decompressed.getUnsigned(DataType.SHORT);
			int[] identifiers = new int[entryCount];
			int[] dSizes = new int[entryCount];
			int[] cSizes = new int[entryCount];
			
			for (int i = 0; i < entryCount; i++) {
				// TODO store these as offsets so we can read individual files without reading all the files
				identifiers[i] = this.decompressed.get(DataType.INTEGER);
				dSizes[i] = this.decompressed.getUnsigned(DataType.TRIPLE);
				cSizes[i] = this.decompressed.getUnsigned(DataType.TRIPLE);
			}
			
			for (int i = 0; i < entryCount; i++) {
				byte[] data = new byte[this.isCompressed() ? dSizes[i] : cSizes[i]];
				this.decompressed.get(data);
				entries.put(identifiers[i], new RT3FolderEntry(identifiers[i], this, new SuperBuffer(ByteBuffer.wrap(data)), dSizes[i]));
			}
		}
		
		return decompressed.duplicate();
	}

	@Override
	public boolean data(SuperBuffer buffer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URI uri() {
		return parent.uri();
	}

	@Override
	public int compressedSize() {
		return compressed.capacity();
	}

	@Override
	public int size() throws IOException {
		return data().capacity();
	}
	
	@Override
	public StoreFolder folder() {
		// TODO support folders within folders
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public StoreFile file(int hash) throws IOException {
		if (entries.isEmpty()) {
			data(); // load data if we haven't already
		}
		
		return entries.get(hash);
	}

	@Override
	public StoreFile file(String name) throws IOException {
		return file(StoreFolder.hash(name));
	}

	@Override
	public boolean exists(int hash) {
		return entries.containsKey(hash);
	}

	@Override
	public boolean exists(String name) {
		return exists(StoreFolder.hash(name));
	}
}
