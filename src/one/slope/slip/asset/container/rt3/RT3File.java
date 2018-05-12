package one.slope.slip.asset.container.rt3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import one.slope.slip.asset.StoreFolder;
import one.slope.slip.asset.StoreFile;
import one.slope.slip.io.SuperBuffer;

public class RT3File implements StoreFile {
	private final RT3Descriptor descriptor;
	private final SuperBuffer compressed;
	private SuperBuffer decompressed;
	
	public RT3File(RT3Descriptor descriptor, SuperBuffer buffer) {
		this.descriptor = descriptor;
		this.compressed = buffer;
	}
	
	@Override
	public SuperBuffer data() throws IOException {
		if (decompressed == null) {
			try {
				GZIPInputStream gzs = new GZIPInputStream(new ByteArrayInputStream(compressed.array()));
				byte[] gzipInputBuffer = new byte[65000];
				int i = 0;
				
				do
				{
					if (i == gzipInputBuffer.length) {
						throw new RuntimeException("buffer overflow!");
					}
					
					int k = gzs.read(gzipInputBuffer, i, gzipInputBuffer.length - i);
					
					if (k == -1) {
						break;
					}
					
					i += k;
				} while(true);
				
				byte[] data = new byte[i];
				System.arraycopy(gzipInputBuffer, 0, data, 0, i);
				decompressed = new SuperBuffer(ByteBuffer.wrap(data));
			}
			catch (ZipException e) {
				if (e.getMessage() == "Not in GZIP format") {
					decompressed = compressed;
					decompressed.flip();
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return decompressed.duplicate();
	}

	@Override
	public boolean data(SuperBuffer buffer) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public URI uri() {
		try {
			return descriptor.uri();
		}
		catch (Exception ex) {
			// TODO better error handling
			ex.printStackTrace();
		}
		
		return null;
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
