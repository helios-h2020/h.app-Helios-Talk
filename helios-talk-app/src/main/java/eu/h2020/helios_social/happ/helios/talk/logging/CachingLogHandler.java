package eu.h2020.helios_social.happ.helios.talk.logging;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
public class CachingLogHandler extends Handler {

	private static final int MAX_RECENT_RECORDS = 100;

	private final Object lock = new Object();
	// Locking: lock
	private final Queue<LogRecord> recent = new LinkedList<>();

	@Override
	public void publish(LogRecord record) {
		synchronized (lock) {
			recent.add(record);
			if (recent.size() > MAX_RECENT_RECORDS) recent.poll();
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
		synchronized (lock) {
			recent.clear();
		}
	}

	public Collection<LogRecord> getRecentLogRecords() {
		synchronized (lock) {
			return new ArrayList<>(recent);
		}
	}
}
