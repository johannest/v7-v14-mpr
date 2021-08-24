package org.vaadin;

import java.io.File;
import java.io.IOException;

import org.vaadin.easyuploads.FileFactory;

/**
 * A factory for creating TempFile objects.
 *
 * @author Shaikni
 */
class TempFileFactory implements FileFactory {

	/* (non-Javadoc)
	 * @see org.vaadin.easyuploads.FileFactory#createFile(java.lang.String, java.lang.String)
	 */
	public File createFile(String fileName, String mimeType) {
		final String tempFileName = "upload_tmpfile_" + System.currentTimeMillis();
		try {
			return File.createTempFile(tempFileName, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
