package at.theduke.spector.client.events;

import at.theduke.spector.Session;

public interface EventWatcher {
	
	/**
	 * @throws Exception
	 */
	public void connect(Session session);
}
