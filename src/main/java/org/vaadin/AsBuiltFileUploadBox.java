package org.vaadin;


import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.Upload.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

/**
 * The Class AsBuiltFileUploadBox.
 */
public class AsBuiltFileUploadBox extends CustomComponent implements Receiver, ProgressListener, FailedListener,
		SucceededListener {
    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(AsBuiltFileUploadBox.class);
    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The as built attachment list view impl. */
	private View asBuiltAttachmentListViewImpl;
	
	/** The os. */
	// Put upload in this memory buffer that grows automatically
	ByteArrayOutputStream os = new ByteArrayOutputStream();

	/** The filename. */
	// Name of the uploaded file
	private String filename;

	/** The progress. */
    ProgressBar progress = new ProgressBar(0.0f);
	private MultiFileUpload multiFileUpload;

	/**
	 * Instantiates a new as built file upload box.
	 *
	 * @param asBuiltAttachmentListViewImpl the as built attachment list view impl
	 */
	public AsBuiltFileUploadBox(View asBuiltAttachmentListViewImpl,  com.vaadin.flow.component.UI flowUI, com.vaadin.flow.component.Component flowComponent) {
		setSizeFull();
		this.asBuiltAttachmentListViewImpl = asBuiltAttachmentListViewImpl;
		// Create the upload component and handle all its events


		try {

            multiFileUpload = new MultiFileUpload( flowUI ) {

				private static final long serialVersionUID = -385909226254598431L;

				@Override
				protected void handleFile(File file, String fileName, String mimeType, long length) {

                    try {
						System.out.println("handleFile: "+file+","+fileName);
//                        asBuiltAttachmentListViewImpl.addOrUpdateItemsToTable(fileName,
//                                new ByteArrayInputStream(Files.readAllBytes(file.toPath())));

                        progress.setVisible(false);
                        // trigger workaround
						if (flowComponent != null) {
							flowComponent.getElement().executeJs("$0.$server.uploadWorkaround();", flowComponent.getElement());
						}
                    } catch (Exception e) {
                        logger.error("Error in uploading file:{} ", e);

                    }

				}
			};

			multiFileUpload.setMaxFileCount(10);
			multiFileUpload.setUploadButtonCaption("arm.attachment.file.upload.button.title");
			multiFileUpload.setImmediate(true);
			/**
			 * This is required to add filter on selected file types (if required based on
			 * FM) multiFileUpload.setAcceptFilter(".pdf, .xlsx, .txt, .docx");
			 */

		} catch (Exception e) {
			logger.error("Exception in uploading file --> {}", e);
		}

		Panel panel = new Panel();
		VerticalLayout panelContent = new VerticalLayout();
		panelContent.setSpacing(true);
		panel.setContent(panelContent);
		panelContent.addComponent(multiFileUpload);

		progress.setVisible(false);

		setCompositionRoot(panel);
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Upload.Receiver#receiveUpload(java.lang.String, java.lang.String)
	 */
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		this.filename = filename;
		os.reset(); // Needed to allow re-uploading
		return os;
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Upload.ProgressListener#updateProgress(long, long)
	 */
	@Override
	public void updateProgress(long readBytes, long contentLength) {
		progress.setVisible(true);
		if (contentLength == -1)
			progress.setIndeterminate(true);
		else {
			progress.setIndeterminate(false);
			progress.setValue(((float) readBytes) / ((float) contentLength));
		}
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Upload.SucceededListener#uploadSucceeded(com.vaadin.ui.Upload.SucceededEvent)
	 */
	@Override
	public void uploadSucceeded(SucceededEvent event) {
		logger.debug("uploadSucceeded- file  name---->" + event.getFilename());

		progress.setVisible(false);

		
		
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Upload.FailedListener#uploadFailed(com.vaadin.ui.Upload.FailedEvent)
	 */
	@Override
	public void uploadFailed(FailedEvent event) {
		Notification.show("Upload failed", Notification.Type.ERROR_MESSAGE);
	}
}
