package one.slope.slip;

import java.net.URL;
import java.net.URLConnection;

import one.slope.slip.asset.StoreFactory;
import one.slope.slip.asset.container.rt3.RT3StoreFactory;
import one.slope.slip.asset.url.StorageURLStreamHandlerFactory;

public class TestURL {
	public static void main(String[] args) throws Exception {
		StoreFactory<?>[] f = { new RT3StoreFactory() };
		URL.setURLStreamHandlerFactory(new StorageURLStreamHandlerFactory(f));
		URL testURL = new URL("storage:C:/cache/test/#/");
		URLConnection connection = testURL.openConnection();
		connection.connect();
		Object o = connection.getContent();
		System.out.println(o);
	}
}
