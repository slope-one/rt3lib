package one.slope.slip;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import one.slope.slip.asset.Store;
import one.slope.slip.asset.StoreFactory;
import one.slope.slip.asset.container.rt3.RT3Store;
import one.slope.slip.asset.container.rt3.RT3StoreFactory;
import one.slope.slip.asset.url.StorageURLStreamHandlerFactory;
import one.slope.slip.io.SuperBuffer;

public class RT3StoreTest {
	public static void main(String[] args) throws Exception {
		Store store = new RT3Store(new URI("file:/I:/377/377%20Client/data/cache/orig_377/"));
		store.open();
		
		SuperBuffer o = store.data(new URI("file:/0/2/loc.idx"));
		System.out.println(o.capacity());
		
		SuperBuffer a = store.data(new URI("file:/0/2/loc.dat"));
		System.out.println(a.capacity());
		
		StoreFactory<?>[] f = { new RT3StoreFactory() };
		URL.setURLStreamHandlerFactory(new StorageURLStreamHandlerFactory(f));
		
		URL testURL = new URL("storage:/I:/377/377%20Client/data/cache/orig_377/#/0/2/loc.idx");
		URLConnection connection = testURL.openConnection();
		connection.connect();
		SuperBuffer b = (SuperBuffer)connection.getContent();
		System.out.println(b.capacity());

		URL testURL2 = new URL("storage:/I:/377/377%20Client/data/cache/orig_377/#/0/2/loc.dat");
		URLConnection connection2 = testURL2.openConnection();
		connection.connect();
		SuperBuffer c = (SuperBuffer)connection2.getContent();
		System.out.println(c.capacity());
	}
}
