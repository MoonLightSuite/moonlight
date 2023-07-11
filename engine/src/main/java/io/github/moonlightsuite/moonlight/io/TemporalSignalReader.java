/**
 *
 */
package io.github.moonlightsuite.moonlight.io;

import java.io.File;
import java.io.IOException;

import io.github.moonlightsuite.moonlight.core.base.MoonLightRecord;
import io.github.moonlightsuite.moonlight.offline.signal.RecordHandler;
import io.github.moonlightsuite.moonlight.offline.signal.Signal;

/**
 * @author loreti
 *
 */
public interface TemporalSignalReader {

	Signal<MoonLightRecord> load(RecordHandler handler, File input) throws IOException, IllegalFileFormatException;
	Signal<MoonLightRecord> load(RecordHandler handler, String input) throws IOException, IllegalFileFormatException;

}
