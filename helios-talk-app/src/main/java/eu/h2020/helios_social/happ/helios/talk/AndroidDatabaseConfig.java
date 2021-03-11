package eu.h2020.helios_social.happ.helios.talk;

import eu.h2020.helios_social.modules.groupcommunications_utils.crypto.KeyStrengthener;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseConfig;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import java.io.File;

import javax.annotation.Nullable;

@NotNullByDefault
class AndroidDatabaseConfig implements DatabaseConfig {

	private final File dbDir, keyDir;
	@Nullable
	private final KeyStrengthener keyStrengthener;

	AndroidDatabaseConfig(File dbDir, File keyDir,
			@Nullable KeyStrengthener keyStrengthener) {
		this.dbDir = dbDir;
		this.keyDir = keyDir;
		this.keyStrengthener = keyStrengthener;
	}

	@Override
	public File getDatabaseDirectory() {
		return dbDir;
	}

	@Override
	public File getDatabaseKeyDirectory() {
		return keyDir;
	}

	@Nullable
	@Override
	public KeyStrengthener getKeyStrengthener() {
		return keyStrengthener;
	}
}
