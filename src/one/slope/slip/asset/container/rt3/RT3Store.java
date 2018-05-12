package one.slope.slip.asset.container.rt3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import one.slope.slip.asset.StoreFolder;
import one.slope.slip.asset.StoreFile;
import one.slope.slip.asset.Store;
import one.slope.slip.io.DataType;
import one.slope.slip.io.SuperBuffer;

public class RT3Store extends Store {
	public static final int DATA_BLOCK_SIZE = 512;
	public static final int DATA_HEADER_SIZE = 8;
	public static final int DATA_SIZE = DATA_HEADER_SIZE + DATA_BLOCK_SIZE;
	
	private RandomAccessFile dataFile;
	private RT3Index[] indices;
	
	public RT3Store(URI directory) {
		super(directory);
	}

	@Override
	public StoreFile file(URI uri) throws IOException {
		RT3Descriptor desc = resolve(uri);
		
		if (desc != null) {
			int expectedIndexID = desc.index() + 1;
			SuperBuffer fileBuffer = new SuperBuffer(desc.size());
			int currentBlockID = desc.start();
			int remaining = desc.size();
			int nextPartID = 0;
			
			while (remaining > 0) {
				dataFile.seek(currentBlockID * DATA_SIZE);
				byte[] tempData = new byte[DATA_HEADER_SIZE];
				dataFile.read(tempData);
				SuperBuffer tempBuffer = new SuperBuffer(ByteBuffer.wrap(tempData));
				int currentFileID = tempBuffer.get(DataType.SHORT);
				int currentPartID = tempBuffer.get(DataType.SHORT);
				int nextBlockID = tempBuffer.get(DataType.TRIPLE);
				int nextIndexID = tempBuffer.get();
				
				if (currentFileID != desc.id()) {
					throw new IOException("Different file ID, index and data appear to be corrupt.");
				}
				else if (currentPartID != nextPartID) {
					throw new IOException("Block ID out of order or wrong file being accessed.");
				}
				else if (nextIndexID != expectedIndexID) {
					throw new IOException("Wrong index ID, must be a different type of file.");
				}
				
				byte[] block = new byte[remaining > DATA_BLOCK_SIZE ? DATA_BLOCK_SIZE : remaining];
				dataFile.read(block);
				fileBuffer.put(block);
				remaining -= block.length;
				currentBlockID = nextBlockID;
				nextPartID++;
			}
			
			fileBuffer.flip();
			StoreFile file = new RT3File(desc, fileBuffer);
			String[] paths = uri.getPath().split("/");
			
			if (paths.length > 3) {
				StoreFolder folder = file.folder();
				return folder.file(paths[3]);
			}
			
			return file;
		}
		
		return null;
	}
	
	// TODO better
	public RT3Descriptor resolve(URI uri) {
		String[] paths = uri.getPath().split("/");
		int index = Integer.parseInt(paths[1]);
		
		if (index >= 0) {
			return indices[index].get(Integer.parseInt(paths[2]));
		}
		
		return null;
	}

	@Override
	public StoreFolder folder(URI uri) throws IOException {
		StoreFile file = file(uri);
		
		if (file != null) {
			return file.folder();
		}
		
		return null;
	}

	@Override
	public boolean exists(URI uri) throws IOException {
		if (resolve(uri) != null) {
			String[] paths = uri.getPath().split("/");
			
			if (paths.length >= 3) {
				return folder(uri) != null;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean save(StoreFile asset) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public boolean save(StoreFolder archive) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public boolean open() throws IOException {
		if (indices == null || indices.length > 0) {
			List<RT3Index> temp = new ArrayList<RT3Index>();
			//int totalEntries = 0;
			
			this.dataFile = new RandomAccessFile(new File(this.directory().getPath() + "/main_file_cache.dat"), "r");
			
			for (int i = 0; i < 255; i++) {
				File indexFile = new File(this.directory().getPath() + "/main_file_cache.idx" + i);
				
				if (indexFile.exists()) {
					FileInputStream fis = new FileInputStream(indexFile);
					byte[] data = new byte[fis.available()];
					fis.read(data);
					fis.close();
					
					RT3Index idx = new RT3Index(i);
					idx.load(new SuperBuffer(ByteBuffer.wrap(data)));
					temp.add(idx);
					//totalEntries += idx.size();
				}
			}
			
			this.indices = temp.toArray(new RT3Index[0]);
		}
		
		return true;
	}

	@Override
	public boolean close() throws IOException {
		dataFile.close();
		return true;
	}

	@Override
	public boolean isOpen() {
		return dataFile != null && dataFile.getChannel().isOpen();
	}

	@Override
	public boolean isFlattened() {
		return false;
	}
}
