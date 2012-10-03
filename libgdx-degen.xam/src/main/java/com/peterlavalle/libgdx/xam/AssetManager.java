package com.peterlavalle.libgdx.xam;

import com.badlogic.gdx.utils.Disposable;

/**
 *
 * @author peter
 */
public class AssetManager {

	public interface IPolicy {

		 <T extends Disposable> void onUpdated(final AssetManager assetManager, Class<T> type, final String id, final T instance);

		 <T extends Disposable> void onUnused(final AssetManager assetManager, Class<T> type, final String id, final T instance);

		 <T extends Disposable> void onUsed(final AssetManager assetManager, Class<T> type, final String id, final T instance, final int count);

		 <T extends Disposable> T onRequestedNew(final AssetManager assetManager, Class<T> type, final String id);
	}

	public interface IDetails {

		 <T extends Disposable> long getModificationTime(Class<T> type, final String id, final T instance);
	}

	public void frameStart() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public <T extends Disposable> T use(final String id) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public void frameStop() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
