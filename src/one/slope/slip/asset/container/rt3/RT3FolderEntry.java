package one.slope.slip.asset.container.rt3;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import one.slope.slip.asset.StoreFile;
import one.slope.slip.asset.StoreFolder;
import one.slope.slip.io.SuperBuffer;

public class RT3FolderEntry implements StoreFile {
	private final SuperBuffer compressed;
	private final int decompressedSize;
	private final StoreFolder parent;
	private final int identifier;
	
	private SuperBuffer decompressed;
	
	public RT3FolderEntry(int identifier, StoreFolder parent, SuperBuffer buffer, int decompressedSize) {
		this.identifier = identifier;
		this.compressed = buffer;
		this.parent = parent;
		this.decompressedSize = decompressedSize;
	}

	@Override
	public SuperBuffer data() throws IOException {
		// do the decompression here, if we actually need the file
		if (decompressed == null) {
			if (!parent.isCompressed()) {
				// if we didn't compress the whole archive, we are compressing individual files
				int cSize = compressed.capacity();
				byte[] compressedData = new byte[cSize + 4];
				byte[] decompressedData = new byte[decompressedSize];
				byte[] comp = new byte[cSize];
				compressed.get(comp);
				System.arraycopy(comp, 0, compressedData, 4, comp.length);
				
				// set up bzip header, since it is always excluded
				compressedData[0] = 'B';
				compressedData[1] = 'Z';
				compressedData[2] = 'h';
				compressedData[3] = '1';
				
				try (DataInputStream is = new DataInputStream(new BZip2CompressorInputStream(new ByteArrayInputStream(compressedData)))) {
					is.readFully(decompressedData);
					decompressed = new SuperBuffer(ByteBuffer.wrap(decompressedData));
				}
			}
			else {
				decompressed = compressed;
			}
		}
		
		return decompressed;
	}

	@Override
	public boolean data(SuperBuffer buffer) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URI uri() {
		URI uri = parent.uri();
		String newPath = uri.getPath() + '/' + identifier;
		return uri.normalize().resolve(newPath);
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
	public StoreFolder folder() throws IOException {
		return new RT3Folder(this, data());
	}
}
