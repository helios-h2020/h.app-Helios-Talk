package eu.h2020.helios_social.happ.helios.talk;

import android.net.TrafficStats;
import android.os.Process;

import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.Service;

import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;

class NetworkUsageLogger implements Service {

	private static final Logger LOG =
			Logger.getLogger(NetworkUsageLogger.class.getName());

	private volatile long startTime, rxBytes, txBytes;

	@Override
	public void startService() {
		startTime = now();
		int uid = Process.myUid();
		rxBytes = TrafficStats.getUidRxBytes(uid);
		txBytes = TrafficStats.getUidTxBytes(uid);
	}

	@Override
	public void stopService() {
		if (LOG.isLoggable(INFO)) {
			long sessionDuration = now() - startTime;
			int uid = Process.myUid();
			long rx = TrafficStats.getUidRxBytes(uid) - rxBytes;
			long tx = TrafficStats.getUidTxBytes(uid) - txBytes;
			LOG.info("Duration " + (sessionDuration / 1000) + " seconds");
			LOG.info("Received " + rx + " bytes");
			LOG.info("Sent " + tx + " bytes");
		}
	}
}
