package one.slope.slip.asset.container.rt3;

import java.net.URL;

import one.slope.slip.asset.StoreFactory;

public class RT3StoreFactory implements StoreFactory<RT3Store> {
	@Override
	public RT3Store get(URL directory) throws Exception {
		return new RT3Store(directory.toURI());
	}
}
