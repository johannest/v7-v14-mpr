package org.vaadin;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.flow.component.PushConfiguration;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.ui.*;
import org.apache.commons.io.FileUtils;
import org.vaadin.easyuploads.DirectoryFileFactory;
import org.vaadin.easyuploads.FileBuffer;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.Html5FileInputSettings;
import org.vaadin.easyuploads.MaxFileSizeExceededException;
import org.vaadin.easyuploads.MultiUpload;
import org.vaadin.easyuploads.MultiUpload.FileDetail;
import org.vaadin.easyuploads.MultiUploadHandler;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.easyuploads.client.AcceptUtil;

import com.vaadin.server.StreamVariable.StreamingEndEvent;
import com.vaadin.server.StreamVariable.StreamingErrorEvent;
import com.vaadin.server.StreamVariable.StreamingProgressEvent;
import com.vaadin.server.StreamVariable.StreamingStartEvent;

/**
 * The Class MultiFileUpload.
 *
 * @author Shaikni
 */

/**
 * MultiFileUpload makes it easier to upload multiple files. MultiFileUpload
 * releases upload button for new uploads immediately when a file is selected
 * (aka parallel uploads). It also displays progress indicators for pending
 * uploads.
 * <p>
 * MultiFileUpload always streams straight to files to keep memory consumption
 * low. To temporary files by default, but this can be overridden with
 * {@link #setFileFactory(FileFactory)} (eg. straight to target directory on the
 * server).
 * <p>
 * Developer handles uploaded files by implementing the abstract
 * {@link #handleFile(File, String, String, long)} method.
 *
 *
 */

@SuppressWarnings("serial")
public abstract class MultiFileUpload extends CssLayout {

    /** The html 5 file input settings. */
    private Map<MultiUpload, Html5FileInputSettings> html5FileInputSettings = new LinkedHashMap<>();

    /** The max file size. */
    private int maxFileSize = -1;

    /** The max file count. */
    private int maxFileCount = -1;

    /** The accept string. */
    private String acceptString = null;

    /** The accept strings. */
    private List<String> acceptStrings = new ArrayList<>();

    /** The accepted uploads. */
    private int acceptedUploads = 0;

    /** The Constant POLL_INTERVAL. */
    private static final int POLL_INTERVAL = 700;

    /** The progress bars. */
    private Layout progressBars;

    /** The uploads. */
    protected CssLayout uploads = new CssLayout() {

        @Override
        protected String getCss(Component c) {
            if (getComponent(uploads.getComponentCount() - 1) != c) {
                return "overflow: hidden; position: absolute;";
            }
            return "overflow:hidden;";
        }

    };

    /** The upload button caption. */
    private String uploadButtonCaption = "...";

    /** The saved poll interval. */
    private Integer savedPollInterval = null;

    private com.vaadin.flow.component.UI flowUI;

    /**
     * Instantiates a new multi file upload.
     */
    public MultiFileUpload( com.vaadin.flow.component.UI flowUI) {
        this.flowUI = flowUI;
        setWidth("200px");
    }

    /**
     * Initialize.
     */
    protected void initialize() {
        addComponent(getprogressBarsLayout());
        uploads.setStyleName("v-multifileupload-uploads");
        addComponent(uploads);
        prepareUpload();
    }

    /**
     * Gets the progress bars layout.
     *
     * @return the progress bars layout
     */
    protected Layout getprogressBarsLayout() {
        if (progressBars == null) {
            progressBars = new VerticalLayout();
        }
        return progressBars;
    }

    /**
     * Prepare upload.
     */
    private void prepareUpload() {
        final FileBuffer receiver = createReceiver();

        final MultiUpload upload = new MultiUpload();
        getHtml5FileInputSettings(upload);
        MultiUploadHandler handler = new MultiUploadHandler() {
            private LinkedList<ProgressBar> indicators;

            public void streamingStarted(StreamingStartEvent event) {
                if (maxFileSize > 0 && event.getContentLength() > maxFileSize) {
                    throw new MaxFileSizeExceededException(event.getContentLength(), maxFileSize);
                }
            }

            public void streamingFinished(StreamingEndEvent event) {

                flowUI.access(() -> {


                    if (!indicators.isEmpty()) {
                        getprogressBarsLayout().removeComponent(indicators.remove(0));
                    }
                    File file = receiver.getFile();
                    handleFile(file, event.getFileName(), event.getMimeType(), event.getBytesReceived());
                    receiver.setValue(null);
                    if (upload.getPendingFileNames().isEmpty()) {
                        uploads.removeComponent(upload);
                        html5FileInputSettings.remove(upload);
                    }
                    resetPollIntervalIfNecessary();
                });
            }

            public void streamingFailed(StreamingErrorEvent event) {
                Logger.getLogger(getClass().getName()).log(Level.FINE, "Streaming failed", event.getException());

                for (ProgressBar progressIndicator : indicators) {
                    getprogressBarsLayout().removeComponent(progressIndicator);
                    --acceptedUploads;
                }
                resetPollIntervalIfNecessary();
            }

            public void onProgress(StreamingProgressEvent event) {
                long readBytes = event.getBytesReceived();
                long contentLength = event.getContentLength();
                float f = (float) readBytes / (float) contentLength;
                indicators.get(0).setValue(f);
            }

            public OutputStream getOutputStream() {
                FileDetail next = upload.getPendingFileNames().iterator().next();
                return receiver.receiveUpload(next.getFileName(), next.getMimeType());
            }

            public void filesQueued(Collection<FileDetail> pendingFileNames) {
                if (indicators == null) {
                    indicators = new LinkedList<>();
                }
                acceptedUploads += pendingFileNames.size();
                for (FileDetail f : pendingFileNames) {
                    ensurePushOrPollingIsEnabled();
                    ProgressBar pi = createProgressIndicator();
                    getprogressBarsLayout().addComponent(pi);
                    pi.setCaption(f.getFileName());
                    pi.setVisible(true);
                    indicators.add(pi);
                }
                upload.setHeight("0px");
                prepareUpload();
            }

            @Override
            public boolean isInterrupted() {
                return false;

            }
        };
        upload.setHandler(handler);
        upload.setButtonCaption(getUploadButtonCaption());
        upload.setStyleName("btn-upload");
        uploads.addComponent(upload);
        flowUI.getPage().executeJs("self.focus();");

    }

    /**
     * Ensure push or polling is enabled.
     */
    protected void ensurePushOrPollingIsEnabled() {
        PushConfiguration pushConfiguration = flowUI.getPushConfiguration();
        PushMode pushMode = pushConfiguration.getPushMode();
        if (pushMode != PushMode.AUTOMATIC) {
            int currentPollInterval = flowUI.getPollInterval();
            if (currentPollInterval == -1 || currentPollInterval > 1000) {
                savedPollInterval = currentPollInterval;
                flowUI.setPollInterval(getPollInterval());
            }
        }
    }

    /**
     * Reset poll interval if necessary.
     */
    protected void resetPollIntervalIfNecessary() {
        if (savedPollInterval != null && getprogressBarsLayout().getComponentCount() == 0) {
            flowUI.setPollInterval(savedPollInterval);
            savedPollInterval = null;
        }
    }

    /**
     * Creates the progress indicator.
     *
     * @return the progress bar
     */
    protected ProgressBar createProgressIndicator() {
        ProgressBar progressIndicator = new ProgressBar();
        progressIndicator.setWidth("100%");
        progressIndicator.setValue(0f);
        return progressIndicator;
    }

    /**
     * Gets the upload button caption.
     *
     * @return the upload button caption
     */
    public String getUploadButtonCaption() {
        return uploadButtonCaption;
    }

    /**
     * Sets the upload button caption.
     *
     * @param uploadButtonCaption the new upload button caption
     */
    @SuppressWarnings("deprecation")
    public void setUploadButtonCaption(String uploadButtonCaption) {
        this.uploadButtonCaption = uploadButtonCaption;
        Iterator<Component> componentIterator = uploads.getComponentIterator();
        while (componentIterator.hasNext()) {
            Component next = componentIterator.next();
            if (next instanceof MultiUpload) {
                MultiUpload upload = (MultiUpload) next;
                if (upload.isVisible()) {
                    upload.setStyleName("ARMButton");
                    upload.setButtonCaption(getUploadButtonCaption());
                }
            }
        }
    }

    /** The file factory. */
    private FileFactory fileFactory;

    /**
     * Gets the file factory.
     *
     * @return the file factory
     */
    public FileFactory getFileFactory() {
        if (fileFactory == null) {
            fileFactory = new TempFileFactory();
        }
        return fileFactory;
    }

    /**
     * Sets the file factory.
     *
     * @param fileFactory the new file factory
     */
    public void setFileFactory(FileFactory fileFactory) {
        this.fileFactory = fileFactory;
    }

    /**
     * Creates the receiver.
     *
     * @return the file buffer
     */
    protected FileBuffer createReceiver() {
        return new FileBuffer(FieldType.FILE) {
            @Override
            public FileFactory getFileFactory() {
                return MultiFileUpload.this.getFileFactory();
            }

            @Override
            public void setLastMimeType(String mimeType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setLastFileName(String fileName) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    /**
     * Gets the poll interval.
     *
     * @return the poll interval
     */
    protected int getPollInterval() {
        return POLL_INTERVAL;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#attach()
     */
    @Override
    public void attach() {
        super.attach();
        if (getComponentCount() == 0) {
            initialize();
        }
    }

    /**
     * Handle file.
     *
     * @param file the file
     * @param fileName the file name
     * @param mimeType the mime type
     * @param length the length
     */
    protected abstract void handleFile(File file, String fileName, String mimeType, long length);

    /**
     * A helper method to set DirectoryFileFactory with given pathname as directory.
     *
     * @param directoryWhereToUpload
     *            the path to directory where files should be uploaded
     */
    public void setRootDirectory(String directoryWhereToUpload) {
        setFileFactory(new DirectoryFileFactory(new File(directoryWhereToUpload)));
    }

    /**
     * Gets the html 5 file input settings.
     *
     * @param upload the upload
     * @return the html 5 file input settings
     */
    private Html5FileInputSettings getHtml5FileInputSettings(MultiUpload upload) {
        if (!html5FileInputSettings.containsKey(upload)) {
            Html5FileInputSettings settings = createHtml5FileInputSettingsIfNecessary(upload);
            html5FileInputSettings.put(upload, settings);
        }
        return html5FileInputSettings.get(upload);
    }

    /**
     * Creates the html 5 file input settings if necessary.
     *
     * @param upload the upload
     * @return the html 5 file input settings
     */
    private Html5FileInputSettings createHtml5FileInputSettingsIfNecessary(MultiUpload upload) {
        Html5FileInputSettings settings = null;
        if (maxFileSize > 0 || acceptString != null || maxFileCount >= 0) {
            settings = new Html5FileInputSettings(upload);
            if (maxFileSize > 0) {
                settings.setMaxFileSize(maxFileSize);
                settings.setMaxFileSizeText(FileUtils.byteCountToDisplaySize(maxFileSize));
            }
            if (acceptString != null) {
                settings.setAcceptFilter(acceptString);
            }
            if (maxFileCount >= 0) {
                settings.setMaxFileCount(getRemainingFileCount());
            }

        }
        return settings;
    }

    /**
     * Gets the accept filter.
     *
     * @return the accept filter
     */
    public String getAcceptFilter() {
        return acceptString;
    }

    /**
     * Sets the accept filter.
     *
     * @param acceptString the new accept filter
     * @see {@link Html5FileInputSettings#setAcceptFilter(String)}
     */
    public void setAcceptFilter(String acceptString) {
        this.acceptString = acceptString;
        acceptStrings.clear();
        acceptStrings.addAll(AcceptUtil.unpack(acceptString));
        for (Entry<MultiUpload, Html5FileInputSettings> entry : html5FileInputSettings.entrySet()) {
            if (entry.getValue() == null) {
                if (acceptString != null) {
                    html5FileInputSettings.put(entry.getKey(), createHtml5FileInputSettingsIfNecessary(entry.getKey()));
                }
            } else {
                entry.getValue().setAcceptFilter(acceptString);
            }
        }
    }

    /**
     * Gets the max file size.
     *
     * @return the max file size
     */
    public int getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Sets the max file size.
     *
     * @param maxFileSize the new max file size
     * @see {@link Html5FileInputSettings#setMaxFileSize(Integer)}
     */
    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
        for (Entry<MultiUpload, Html5FileInputSettings> entry : html5FileInputSettings.entrySet()) {
            if (entry.getValue() == null) {
                if (maxFileSize > 0) {
                    html5FileInputSettings.put(entry.getKey(), createHtml5FileInputSettingsIfNecessary(entry.getKey()));
                }
            } else {
                entry.getValue().setMaxFileSize(maxFileSize);
                if (maxFileSize > 0) {
                    entry.getValue().setMaxFileSizeText(FileUtils.byteCountToDisplaySize(maxFileSize));
                } else {
                    entry.getValue().setMaxFileSizeText("not set");
                }
            }
        }
    }

    /**
     * Sets the max file count.
     *
     * @param maxFileCount the new max file count
     */
    public void setMaxFileCount(int maxFileCount) {
        this.maxFileCount = maxFileCount;
        int uploadCount = html5FileInputSettings.entrySet().size();
        int processing = 0;
        for (Entry<MultiUpload, Html5FileInputSettings> entry : html5FileInputSettings.entrySet()) {
            ++processing;
            if (entry.getValue() == null) {
                if (maxFileCount >= 0) {
                    html5FileInputSettings.put(entry.getKey(), createHtml5FileInputSettingsIfNecessary(entry.getKey()));
                }
            } else {
                if (processing == uploadCount) {
                    // only the latest upload can accept more files
                    entry.getValue().setMaxFileCount(getRemainingFileCount());
                } else {
                    entry.getValue().setMaxFileCount(0);
                }
            }
        }
    }

    /**
     * Gets the remaining file count.
     *
     * @return the remaining file count
     */
    public Integer getRemainingFileCount() {
        return maxFileCount - acceptedUploads;
    }

    /**
     * On max size exceeded.
     *
     * @param contentLength the content length
     */
    public void onMaxSizeExceeded(long contentLength) {
        Notification.show("Max size exceeded " + FileUtils.byteCountToDisplaySize(contentLength) + " > "
                + FileUtils.byteCountToDisplaySize(maxFileSize), Notification.Type.ERROR_MESSAGE);
    }

    /**
     * On file type mismatch.
     */
    public void onFileTypeMismatch() {
        Notification.show("File type mismatch, accepted: " + acceptString, Notification.Type.ERROR_MESSAGE);
    }

    /**
     * On file count exceeded.
     */
    public void onFileCountExceeded() {
        Notification.show("File count exceeded", Notification.Type.ERROR_MESSAGE);
    }
}
