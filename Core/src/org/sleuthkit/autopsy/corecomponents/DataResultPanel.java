/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013-2018 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.corecomponents;

import com.google.common.eventbus.Subscribe;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeMemberEvent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.core.UserPreferences;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContent;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataResult;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataResultViewer;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.BaseChildFactory;
import org.sleuthkit.autopsy.datamodel.BaseChildFactory.PageChangeEvent;
import org.sleuthkit.autopsy.datamodel.BaseChildFactory.PageCountChangeEvent;
import org.sleuthkit.autopsy.datamodel.BaseChildFactory.PageSizeChangeEvent;
import org.sleuthkit.autopsy.datamodel.NodeSelectionInfo;
import org.sleuthkit.autopsy.ingest.IngestManager;
import org.sleuthkit.autopsy.mainui.datamodel.AnalysisResultSearchParam;
import org.sleuthkit.autopsy.mainui.datamodel.DataArtifactSearchParam;
import org.sleuthkit.autopsy.mainui.datamodel.FileTypeExtensionsSearchParams;
import org.sleuthkit.autopsy.mainui.datamodel.FileTypeMimeSearchParams;
import org.sleuthkit.autopsy.mainui.nodes.SearchResultRootNode;
import org.sleuthkit.autopsy.mainui.datamodel.SearchResultsDTO;
import org.sleuthkit.autopsy.mainui.nodes.SearchResultSupport;

/**
 * A result view panel is a JPanel with a JTabbedPane child component that
 * contains a collection of result viewers and implements the DataResult
 * interface. The result viewers in a result view panel are either supplied
 * during construction of the panel or are obtained from the result viewer
 * extension point (DataResultViewer service providers).
 *
 * A result view panel provides an implementation of the setNode API of the the
 * DataResult interface that pushes a given NetBeans Node into its child result
 * viewers via the DataResultViewer.setNode API. The result viewers are
 * responsible for providing a view of the application data represented by the
 * node. A typical result viewer is a JPanel that displays the child nodes of
 * the given node using a NetBeans explorer view child component.
 *
 * All result view panels should be child components of top components that are
 * explorer manager providers. The parent top component is expected to expose a
 * lookup maintained by its explorer manager to the actions global context. The
 * child result view panel will then find the parent top component's explorer
 * manager at runtime, so that it can act as an explorer manager provider for
 * its child result viewers. This connects the nodes displayed in the result
 * viewers to the actions global context.
 *
 * Result view panels can be constructed so that they push single node
 * selections in the child result viewers to a content view (implements
 * DataContent). The content view could be the "main" content view
 * (DataContentTopComponent) that is normally docked into the lower right hand
 * side of the main application window, or it could be a custom content view.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
public class DataResultPanel extends javax.swing.JPanel implements DataResult, ChangeListener, ExplorerManager.Provider {

    private static final Logger logger = Logger.getLogger(DataResultPanel.class.getName());

    private final Map<String, BaseChildFactoryPager> nodeNameToPageCountListenerMap = new ConcurrentHashMap<>();
    private static final long serialVersionUID = 1L;
    private static final int NO_TAB_SELECTED = -1;
    private static final String PLEASE_WAIT_NODE_DISPLAY_NAME = NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.pleasewaitNodeDisplayName");
    private final boolean isMain;
    private final List<DataResultViewer> resultViewers;
    private final ExplorerManagerListener explorerManagerListener;
    private RootNodeListener rootNodeListener = null;
    private DataContent contentView;
    private ExplorerManager explorerManager;
    private Node currentRootNode;
    private boolean listeningToTabbedPane;
    private BaseChildFactoryPager pagingSupport = null;
    private final SearchResultSupport searchResultSupport = new SearchResultSupport(UserPreferences.getResultsTablePageSize());

    private final PreferenceChangeListener pageSizeListener = (PreferenceChangeEvent evt) -> {
        if (evt.getKey().equals(UserPreferences.RESULTS_TABLE_PAGE_SIZE)) {
            int newPageSize = UserPreferences.getResultsTablePageSize();

            nodeNameToPageCountListenerMap.values().forEach((ps) -> {
                ps.postPageSizeChangeEvent();
            });

            try {
                if (this.searchResultSupport.getCurrentSearchResults() != null) {
                    displaySearchResults(this.searchResultSupport.updatePageSize(newPageSize), false);
                } else {
                    this.searchResultSupport.setPageSize(newPageSize);
                    setNode(this.currentRootNode);
                }

            } catch (IllegalArgumentException | ExecutionException ex) {
                logger.log(Level.WARNING, "There was an error while updating page size", ex);
            }
        }
    };

    private static final Set<Case.Events> CASE_EVENTS_OF_INTEREST = EnumSet.of(Case.Events.DATA_SOURCE_ADDED, Case.Events.CURRENT_CASE);

    private final PropertyChangeListener caseEventListener = evt -> {
        String evtName = evt.getPropertyName();
        if (Case.Events.DATA_SOURCE_ADDED.toString().equals(evtName)) {
            refreshSearchResultChildren();
        } else if (Case.Events.CURRENT_CASE.toString().equals(evtName) && evt.getNewValue() == null) {
            nodeNameToPageCountListenerMap.clear();
        }
    };

    private final PropertyChangeListener weakCaseEventListener = WeakListeners.propertyChange(caseEventListener, null);

    private static final Set<IngestManager.IngestModuleEvent> INGEST_MODULE_EVENTS = EnumSet.of(
            IngestManager.IngestModuleEvent.FILE_DONE,
            IngestManager.IngestModuleEvent.CONTENT_CHANGED,
            IngestManager.IngestModuleEvent.DATA_ADDED);

    private final PropertyChangeListener ingestModuleListener = evt -> {
        if (this.searchResultSupport.isRefreshRequired(evt)) {
            refreshSearchResultChildren();
        }
    };

    private final PropertyChangeListener weakIngestModuleListener = WeakListeners.propertyChange(ingestModuleListener, null);

    private static final Set<IngestManager.IngestJobEvent> INGEST_JOB_EVENTS = EnumSet.of(
            IngestManager.IngestJobEvent.COMPLETED,
            IngestManager.IngestJobEvent.CANCELLED);

    private final PropertyChangeListener ingestJobListener = (PropertyChangeEvent evt) -> {
        String eventType = evt.getPropertyName();
        if (eventType.equals(IngestManager.IngestJobEvent.COMPLETED.toString())
                || eventType.equals(IngestManager.IngestJobEvent.CANCELLED.toString())) {
            refreshSearchResultChildren();
        }
    };

    private final PropertyChangeListener weakIngestJobListener = WeakListeners.propertyChange(ingestJobListener, null);

    /**
     * Creates and opens a Swing JPanel with a JTabbedPane child component that
     * contains instances of the result viewers (DataResultViewer) provided by
     * the result viewer extension point (service providers that implement
     * DataResultViewer). The result view panel will push single node selections
     * from its child result viewers to the "main" content view that is normally
     * docked into the lower right hand side of the main application window.
     *
     * @param title           The title for the result view panel.
     * @param description     Descriptive text about the source of the nodes
     *                        displayed.
     * @param currentRootNode The current root (parent) node for the nodes
     *                        displayed. May be changed by calling setNode.
     * @param childNodeCount  The cardinality of the root node's children.
     *
     * @return A result view panel.
     */
    public static DataResultPanel createInstance(String title, String description, Node currentRootNode, int childNodeCount) {
        DataResultPanel resultPanel = new DataResultPanel(title, false, Collections.emptyList(), DataContentTopComponent.findInstance());
        createInstanceCommon(title, description, currentRootNode, childNodeCount, resultPanel);
        resultPanel.open();
        return resultPanel;
    }

    /**
     * Creates and opens a Swing JPanel with a JTabbedPane child component that
     * contains a given collection of result viewers (DataResultViewer) instead
     * of the result viewers provided by the results viewer extension point. The
     * result view panel will push single node selections from its child result
     * viewers to the "main" content view that is normally docked into the lower
     * right hand side of the main application window.
     *
     * @param title           The title for the result view panel.
     * @param description     Descriptive text about the source of the nodes
     *                        displayed.
     * @param currentRootNode The current root (parent) node for the nodes
     *                        displayed. May be changed by calling setNode.
     * @param childNodeCount  The cardinality of the root node's children.
     * @param viewers         A collection of result viewers to use instead of
     *                        the result viewers provided by the results viewer
     *                        extension point.
     *
     * @return A result view panel.
     */
    public static DataResultPanel createInstance(String title, String description, Node currentRootNode, int childNodeCount, Collection<DataResultViewer> viewers) {
        DataResultPanel resultPanel = new DataResultPanel(title, false, viewers, DataContentTopComponent.findInstance());
        createInstanceCommon(title, description, currentRootNode, childNodeCount, resultPanel);
        resultPanel.open();
        return resultPanel;
    }

    /**
     * Creates and opens a Swing JPanel with a JTabbedPane child component that
     * contains instances of the result viewers (DataResultViewer) provided by
     * the result viewer extension point (service providers that implement
     * DataResultViewer). The result view panel will push single node selections
     * from its child result viewers to the supplied content view, which can be
     * null if a content view is not needed.
     *
     * @param title             The title for the result view panel.
     * @param description       Descriptive text about the source of the nodes
     *                          displayed.
     * @param currentRootNode   The current root (parent) node for the nodes
     *                          displayed. May be changed by calling setNode.
     * @param childNodeCount    The cardinality of the root node's children.
     * @param customContentView A custom content view to use instead of the
     *                          "main" content view that is normally docked into
     *                          the lower right hand side of the main
     *                          application window. May be null, if no content
     *                          view is needed.
     *
     * @return A result view panel.
     */
    public static DataResultPanel createInstance(String title, String description, Node currentRootNode, int childNodeCount, DataContent customContentView) {
        DataResultPanel resultPanel = new DataResultPanel(title, false, Collections.emptyList(), customContentView);
        createInstanceCommon(title, description, currentRootNode, childNodeCount, resultPanel);
        resultPanel.open();
        return resultPanel;
    }

    /**
     * Creates, but does not open, a Swing JPanel with a JTabbedPane child
     * component that contains instances of the result viewers
     * (DataResultViewer) provided by the result viewer extension point (service
     * providers that implement DataResultViewer). The result view panel will
     * push single node selections from its child result viewers to the supplied
     * custom content view.
     *
     * @param title             The title for the result view panel.
     * @param description       Descriptive text about the source of the nodes
     *                          displayed.
     * @param currentRootNode   The current root (parent) node for the nodes
     *                          displayed. May be changed by calling setNode.
     * @param childNodeCount    The cardinality of the root node's children.
     * @param customContentView A content view to use in place of the default
     *                          content view.
     *
     * @return A result view panel.
     */
    public static DataResultPanel createInstanceUninitialized(String title, String description, Node currentRootNode, int childNodeCount, DataContent customContentView) {
        DataResultPanel resultPanel = new DataResultPanel(title, false, Collections.emptyList(), customContentView);
        createInstanceCommon(title, description, currentRootNode, childNodeCount, resultPanel);
        return resultPanel;
    }

    /**
     * Executes code common to all of the result view panel factory methods.
     *
     * @param title           The title for the result view panel.
     * @param description     Descriptive text about the source of the nodes
     *                        displayed.
     * @param currentRootNode The current root (parent) node for the nodes
     *                        displayed. May be changed by calling setNode.
     * @param childNodeCount  The cardinality of the root node's children.
     * @param resultViewPanel A new results view panel.
     */
    private static void createInstanceCommon(String title, String description, Node currentRootNode, int childNodeCount, DataResultPanel resultViewPanel) {
        resultViewPanel.setTitle(title);
        resultViewPanel.setName(title);
        resultViewPanel.setNumberOfChildNodes(childNodeCount);
        resultViewPanel.setNode(currentRootNode);
        resultViewPanel.setPath(description);
    }

    /**
     * Constructs a Swing JPanel with a JTabbedPane child component that
     * contains a collection of result viewers that is either supplied or
     * provided by the result viewer extension point.
     *
     * @param title       The title of the result view panel.
     * @param isMain      Whether or not the result view panel is the "main"
     *                    instance of the panel that resides in the "main"
     *                    results view (DataResultTopComponent) that is normally
     *                    docked into the upper right hand side of the main
     *                    application window.
     * @param viewers     A collection of result viewers to use instead of the
     *                    result viewers provided by the results viewer
     *                    extension point, may be empty.
     * @param contentView A content view to into which to push single node
     *                    selections in the child result viewers, may be null.
     */
    DataResultPanel(String title, boolean isMain, Collection<DataResultViewer> viewers, DataContent contentView) {
        this.setTitle(title);
        this.isMain = isMain;
        this.contentView = contentView;
        this.resultViewers = new ArrayList<>(viewers);
        this.explorerManagerListener = new ExplorerManagerListener();
        initComponents();
    }

    /**
     * Gets the preferred identifier for this result view panel in the window
     * system.
     *
     * @return The preferred identifier.
     */
    @Override
    public String getPreferredID() {
        return getName();
    }

    /**
     * Sets the title of this result view panel.
     *
     * @param title The title.
     */
    @Override
    public void setTitle(String title) {
        setName(title);
    }

    /**
     * Sets the descriptive text about the source of the nodes displayed in this
     * result view panel.
     *
     * @param description The text to display.
     */
    @Override
    public void setPath(String description) {
        this.descriptionLabel.setText(description);
    }

    /**
     * Adds a results viewer to this result view panel.
     *
     * @param resultViewer The results viewer.
     */
    public void addResultViewer(DataResultViewer resultViewer) {
        resultViewers.add(resultViewer);
        resultViewerTabs.addTab(resultViewer.getTitle(), resultViewer.getComponent());
    }

    /**
     * Gets the result viewers for this result view panel.
     *
     * @return A list of result viewers.
     */
    @Override
    public List<DataResultViewer> getViewers() {
        return Collections.unmodifiableList(resultViewers);
    }

    /**
     * Sets the content view for this result view panel. Needs to be called
     * before the first call to open.
     *
     * @param customContentView A content view to use in place of the default
     *                          content view.
     */
    public void setContentViewer(DataContent customContentView) {
        this.contentView = customContentView;
    }

    /**
     * Opens this result view panel. Should be called by a parent top component
     * when the top component is opened.
     */
    public void open() {
        /*
         * The parent top component is expected to be an explorer manager
         * provider that exposes a lookup maintained by its explorer manager to
         * the actions global context. The child result view panel will then
         * find the parent top component's explorer manager at runtime, so that
         * it can act as an explorer manager provider for its child result
         * viewers. This connects the nodes displayed in the result viewers to
         * the actions global context.
         */
        if (this.explorerManager == null) {
            this.explorerManager = ExplorerManager.find(this);
            this.explorerManager.addPropertyChangeListener(this.explorerManagerListener);
        }

        /*
         * Load either the supplied result viewers or the result viewers
         * provided by the result viewer extension point into the tabbed pane.
         * If loading from the extension point and distinct result viewer
         * instances MUST be created if this is not the "main" result view.
         */
        if (this.resultViewerTabs.getTabCount() == 0) {
            if (this.resultViewers.isEmpty()) {
                for (DataResultViewer resultViewer : Lookup.getDefault().lookupAll(DataResultViewer.class
                )) {
                    if (this.isMain) {
                        this.resultViewers.add(resultViewer);
                    } else {
                        this.resultViewers.add(resultViewer.createInstance());
                    }
                }
            }
            this.resultViewers.forEach((resultViewer) -> resultViewerTabs.addTab(resultViewer.getTitle(), resultViewer.getComponent()));
        }

        initListeners();

        this.setVisible(true);
    }

    /**
     * Initializes autopsy event listeners.
     */
    private void initListeners() {
        UserPreferences.addChangeListener(this.pageSizeListener);
        Case.addEventTypeSubscriber(CASE_EVENTS_OF_INTEREST, this.weakCaseEventListener);
        IngestManager.getInstance().addIngestModuleEventListener(INGEST_MODULE_EVENTS, this.weakIngestModuleListener);
        IngestManager.getInstance().addIngestJobEventListener(INGEST_JOB_EVENTS, weakIngestJobListener);
    }

    /**
     * Unregisters this panel from autopsy event listeners.
     */
    private void closeListeners() {
        UserPreferences.removeChangeListener(this.pageSizeListener);
        Case.removeEventTypeSubscriber(EnumSet.of(Case.Events.CURRENT_CASE), this.weakCaseEventListener);
        IngestManager.getInstance().removeIngestModuleEventListener(INGEST_MODULE_EVENTS, this.weakIngestModuleListener);
        IngestManager.getInstance().removeIngestJobEventListener(INGEST_JOB_EVENTS, weakIngestJobListener);
    }

    /**
     * Sets the current root node for this result view panel. The child nodes of
     * the current root node will be displayed in the child result viewers. For
     * the "main" panel, the root node is the currently selected node in the
     * application tree view docked into the left side of the main application
     * window.
     *
     * @param rootNode The root node for this panel, may be null if the panel is
     *                 to be reset.
     */
    @Override
    public void setNode(Node rootNode) {
        setNode(rootNode, true);
    }

    private void setNode(Node rootNode, boolean fullRefresh) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setNode(rootNode, fullRefresh));
            return;
        }

        if (this.currentRootNode != null && this.rootNodeListener != null) {
            this.currentRootNode.removeNodeListener(rootNodeListener);
        }

        /*
         * Deferring becoming a listener to the tabbed pane until this point
         * eliminates handling a superfluous stateChanged event during
         * construction.
         */
        if (listeningToTabbedPane == false) {
            resultViewerTabs.addChangeListener(this);
            listeningToTabbedPane = true;
        }

        this.currentRootNode = rootNode;

        // if search result node clear out base child factory paging
        if (this.currentRootNode instanceof SearchResultRootNode) {
            this.pagingSupport = null;
        } else {
            // otherwise clear out search result support parameters
            this.searchResultSupport.clearSearchParameters();

            // if there is a node, set up paging
            if (this.currentRootNode != null) {
                this.pagingSupport
                        = this.nodeNameToPageCountListenerMap.computeIfAbsent(this.currentRootNode.getName(), (name) -> {
                            BaseChildFactoryPager listener = new BaseChildFactoryPager(name);
                            BaseChildFactory.register(name, listener);
                            return listener;
                        });

                if (fullRefresh && this.pagingSupport.getCurrentPageIdx() != 0) {
                    this.pagingSupport.setCurrentPageIdx(0);
                }
            } else {
                this.pagingSupport = null;
            }
        }

        if (this.currentRootNode != null) {
            /*
             * The only place we reset the rootNodeListener allowing the
             * contents of the results tab represented by this node to be
             * changed a single time before it is necessary to reset it again.
             * Necessary when transitioning from "Please wait..." node to having
             * contents.
             */
            rootNodeListener = new RootNodeListener(fullRefresh);
            this.currentRootNode.addNodeListener(rootNodeListener);
        }

        if (fullRefresh) {
            this.resultViewers.forEach((viewer) -> {
                viewer.resetComponent();
            });
        }

        setupTabs(this.currentRootNode, fullRefresh);

        if (fullRefresh && this.currentRootNode != null) {
            long childrenCount = (this.searchResultSupport.getCurrentSearchResults() != null)
                    ? this.searchResultSupport.getCurrentSearchResults().getTotalResultsCount()
                    : this.currentRootNode.getChildren().getNodesCount();
            this.numberOfChildNodesLabel.setText(Long.toString(childrenCount));
        }

        this.numberOfChildNodesLabel.setVisible(true);

        updatePagingComponents();
    }

    /**
     * Gets the root node of this result view panel. For the "main" panel, the
     * root node is the currently selected node in the application tree view
     * docked into the left side of the main application window.
     *
     * @return The root node.
     */
    public Node getRootNode() {
        return currentRootNode;
    }

    /**
     * Sets the label text that displays the number of the child nodes displayed
     * by this result view panel's result viewers.
     *
     * @param numberOfChildNodes The number of child nodes.
     */
    public void setNumberOfChildNodes(Integer numberOfChildNodes) {
        this.numberOfChildNodesLabel.setText(Integer.toString(numberOfChildNodes));
    }

    /**
     * Selects the given child nodes of the root node in this panel's result
     * viewers.
     *
     * @param selectedNodes The child nodes to be selected.
     */
    public void setSelectedNodes(Node[] selectedNodes) {
        this.resultViewers.forEach((viewer) -> viewer.setSelectedNodes(selectedNodes));
    }

    /**
     * Returns the data result viewer tab index to select based on selection
     * info or first available viewer.
     *
     * @param selectedNode The selected node.
     *
     * @return The tab index.
     */
    private int getPriorityTabIdx(Node selectedNode) {
        /*
         * If the selected node has a child to be selected, default the selected
         * tab to the table result viewer. Otherwise, use the last selected tab,
         * if it is enabled. If not, select the first enabled tab that can be
         * found.
         */
        int tabToSelect = NO_TAB_SELECTED;
        if (selectedNode instanceof TableFilterNode) {
            NodeSelectionInfo selectedChildInfo = ((TableFilterNode) selectedNode).getChildNodeSelectionInfo();
            if (null != selectedChildInfo) {
                for (int i = 0; i < resultViewers.size(); ++i) {
                    if (resultViewers.get(i) instanceof DataResultViewerTable && resultViewerTabs.isEnabledAt(i)) {
                        tabToSelect = i;
                    }
                }
            }
        }
        if (tabToSelect == NO_TAB_SELECTED) {
            if ((tabToSelect == NO_TAB_SELECTED) || (!resultViewerTabs.isEnabledAt(tabToSelect))) {
                for (int i = 0; i < resultViewerTabs.getTabCount(); ++i) {
                    if (resultViewerTabs.isEnabledAt(i)) {
                        tabToSelect = i;
                        break;
                    }
                }
            }
        }

        return tabToSelect;
    }

    /**
     * Sets the state of the child result viewers, based on a selected root
     * node.
     *
     * @param selectedNode The selected node.
     * @param fullReset    Whether or not to perform a full reset (including the
     *                     tab index).
     */
    private void setupTabs(Node selectedNode, boolean fullReset) {
        if (fullReset) {
            /*
         * Enable or disable the result viewer tabs based on whether or not the
         * corresponding results viewer supports display of the selected node.
             */
            for (int i = 0; i < resultViewerTabs.getTabCount(); i++) {
                if (resultViewers.get(i).isSupported(selectedNode)) {
                    resultViewerTabs.setEnabledAt(i, true);
                } else {
                    resultViewerTabs.setEnabledAt(i, false);
                }
            }
        }

        int tabToSelect = fullReset
                ? getPriorityTabIdx(selectedNode)
                : resultViewerTabs.getSelectedIndex();

        /*
         * If there is a tab to select, do so, and push the selected node to the
         * corresponding result viewer.
         */
        if (tabToSelect != NO_TAB_SELECTED) {
            resultViewerTabs.setSelectedIndex(tabToSelect);
            resultViewers.get(tabToSelect).setNode(selectedNode, this.searchResultSupport.getCurrentSearchResults());
        }
    }

    /**
     * Responds to a tab selection changed event by setting the root node of the
     * corresponding result viewer.
     *
     * @param event The change event.
     */
    @Override
    public void stateChanged(ChangeEvent event) {
        JTabbedPane pane = (JTabbedPane) event.getSource();
        int currentTab = pane.getSelectedIndex();
        if (currentTab != DataResultPanel.NO_TAB_SELECTED) {
            DataResultViewer currentViewer = this.resultViewers.get(currentTab);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (this.searchResultSupport.getCurrentSearchResults() != null) {
                currentViewer.setNode(currentRootNode, this.searchResultSupport.getCurrentSearchResults());
            } else {
                currentViewer.setNode(currentRootNode);
            }
            this.setCursor(null);
        }
    }

    /**
     * Indicates whether or not this panel can be closed at the time of the
     * call.
     *
     * @return True or false.
     */
    public boolean canClose() {
        /*
         * If this is the "main" panel, only allow it to be closed when no case
         * is open or no there are no data sources in the current case.
         */
        Case openCase;
        try {
            openCase = Case.getCurrentCaseThrows();
        } catch (NoCurrentCaseException ex) {
            return true;
        }
        return (!this.isMain) || openCase.hasData() == false;
    }

    /**
     * Closes down the component. Intended to be called by the parent top
     * component when it is closed.
     */
    void close() {
        if (explorerManager != null && explorerManagerListener != null) {
            explorerManager.removePropertyChangeListener(explorerManagerListener);
            explorerManager = null;
        }

        this.resultViewers.forEach((viewer) -> viewer.setNode(null));

        if (!this.isMain) {
            this.resultViewers.forEach(DataResultViewer::clearComponent);
            this.setLayout(null);
            this.removeAll();
            this.setVisible(false);
        }

        closeListeners();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;

    }

    /**
     * Responds to node selection change events from the explorer manager of
     * this panel's parent top component. The selected nodes are passed to the
     * content view. This is how the results view and the content view are kept
     * in sync. It is therefore required that all of the result viewers in this
     * panel use the explorer manager of the parent top component. This supports
     * this way of passing the selection to the content view, plus the exposure
     * of the selection to through the actions global context, which is needed
     * for multiple selection.
     */
    private class ExplorerManagerListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES) && contentView != null) {
                /*
                 * Pass a single node selection in a result viewer to the
                 * content view. Note that passing null to the content view
                 * signals that either multiple nodes are selected, or a
                 * previous selection has been cleared. This is important to the
                 * content view, since its child content viewers only work for a
                 * single node.
                 */
                Node[] selectedNodes = explorerManager.getSelectedNodes();
                if (selectedNodes.length == 1) {
                    contentView.setNode(selectedNodes[0]);
                } else {
                    contentView.setNode(null);
                }
            }
        }
    }

    /**
     * Responds to changes in the root node due to asynchronous child node
     * creation. This listener allows for the tabs of the result viewer to be
     * set up again after the "Please wait..." node has ended and actual content
     * should be displayed in the table.
     */
    private class RootNodeListener extends NodeAdapter {

        //it is assumed we are still waiting for data when the node is initially constructed
        private volatile boolean waitingForData = true;
        private final boolean fullReset;

        public RootNodeListener(boolean fullReset) {
            this.fullReset = fullReset;
        }

        @Override
        public void childrenAdded(final NodeMemberEvent nme) {
            Node[] delta = nme.getDelta();
            updateMatches();

            /*
             * Ensures that after the initial call to setupTabs in the
             * DataResultPanel.setNode method that we only call setupTabs one
             * additional time. This is to account for the transition that is
             * possible from a "Please wait..." node or a tab with no results in
             * it and a tab containing data and thereby having all of it's
             * columns.
             */
            if (waitingForData && containsReal(delta)) {
                waitingForData = false;
                if (SwingUtilities.isEventDispatchThread()) {
                    setupTabs(nme.getNode(), this.fullReset);
                } else {
                    SwingUtilities.invokeLater(() -> {
                        setupTabs(nme.getNode(), this.fullReset);
                    });
                }
            }
        }

        private boolean containsReal(Node[] delta) {
            for (Node n : delta) {
                if (!n.getDisplayName().equals(PLEASE_WAIT_NODE_DISPLAY_NAME)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Updates the Number of Matches label on the DataResultPanel.
         *
         */
        private void updateMatches() {
            if (DataResultPanel.this.searchResultSupport.getCurrentSearchResults() != null) {
                long resultCount = DataResultPanel.this.searchResultSupport.getCurrentSearchResults().getTotalResultsCount();
                if (resultCount > Integer.MAX_VALUE) {
                    resultCount = Integer.MAX_VALUE;
                }

                setNumMatches((int) resultCount);
            } else if (currentRootNode != null && currentRootNode.getChildren() != null) {
                setNumMatches(currentRootNode.getChildren().getNodesCount());
            }
        }

        @Override
        public void childrenRemoved(NodeMemberEvent nme) {
            updateMatches();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        descriptionLabel = new javax.swing.JLabel();
        numberOfChildNodesLabel = new javax.swing.JLabel();
        javax.swing.JLabel matchLabel = new javax.swing.JLabel();
        javax.swing.JLabel pageLabel = new javax.swing.JLabel();
        pageNumLabel = new javax.swing.JLabel();
        pagesLabel = new javax.swing.JLabel();
        pagePrevButton = new javax.swing.JButton();
        pageNextButton = new javax.swing.JButton();
        gotoPageLabel = new javax.swing.JLabel();
        gotoPageTextField = new javax.swing.JTextField();
        resultViewerTabs = new javax.swing.JTabbedPane();
        javax.swing.JPanel horizontalSpacer = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(0, 5));
        setPreferredSize(new java.awt.Dimension(5, 5));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.descriptionLabel.text")); // NOI18N
        descriptionLabel.setMaximumSize(new java.awt.Dimension(32767, 16));
        descriptionLabel.setMinimumSize(new java.awt.Dimension(50, 14));
        descriptionLabel.setPreferredSize(new java.awt.Dimension(32767, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(descriptionLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(numberOfChildNodesLabel, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.numberOfChildNodesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(numberOfChildNodesLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(matchLabel, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.matchLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(matchLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(pageLabel, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.pageLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(pageLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(pageNumLabel, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.pageNumLabel.text")); // NOI18N
        pageNumLabel.setMaximumSize(null);
        pageNumLabel.setMinimumSize(null);
        pageNumLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(pageNumLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(pagesLabel, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.pagesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(pagesLabel, gridBagConstraints);

        pagePrevButton.setBackground(null);
        pagePrevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_back.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(pagePrevButton, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.pagePrevButton.text")); // NOI18N
        pagePrevButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_back_disabled.png"))); // NOI18N
        pagePrevButton.setFocusable(false);
        pagePrevButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pagePrevButton.setIconTextGap(0);
        pagePrevButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pagePrevButton.setMaximumSize(new java.awt.Dimension(22, 23));
        pagePrevButton.setMinimumSize(new java.awt.Dimension(22, 23));
        pagePrevButton.setPreferredSize(new java.awt.Dimension(22, 23));
        pagePrevButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_back_hover.png"))); // NOI18N
        pagePrevButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pagePrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pagePrevButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(pagePrevButton, gridBagConstraints);

        pageNextButton.setBackground(null);
        pageNextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_forward.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(pageNextButton, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.pageNextButton.text")); // NOI18N
        pageNextButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_forward_disabled.png"))); // NOI18N
        pageNextButton.setFocusable(false);
        pageNextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pageNextButton.setIconTextGap(0);
        pageNextButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pageNextButton.setMaximumSize(new java.awt.Dimension(22, 23));
        pageNextButton.setMinimumSize(new java.awt.Dimension(22, 23));
        pageNextButton.setPreferredSize(new java.awt.Dimension(22, 23));
        pageNextButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_forward_hover.png"))); // NOI18N
        pageNextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pageNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pageNextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(pageNextButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(gotoPageLabel, org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.gotoPageLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(gotoPageLabel, gridBagConstraints);

        gotoPageTextField.setText(org.openide.util.NbBundle.getMessage(DataResultPanel.class, "DataResultPanel.gotoPageTextField.text")); // NOI18N
        gotoPageTextField.setMaximumSize(new java.awt.Dimension(32767, 22));
        gotoPageTextField.setMinimumSize(new java.awt.Dimension(50, 22));
        gotoPageTextField.setPreferredSize(new java.awt.Dimension(50, 22));
        gotoPageTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoPageTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(gotoPageTextField, gridBagConstraints);

        resultViewerTabs.setMinimumSize(new java.awt.Dimension(0, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(resultViewerTabs, gridBagConstraints);

        horizontalSpacer.setMaximumSize(new java.awt.Dimension(0, 0));
        horizontalSpacer.setMinimumSize(new java.awt.Dimension(20, 0));
        horizontalSpacer.setPreferredSize(new java.awt.Dimension(20, 0));

        javax.swing.GroupLayout horizontalSpacerLayout = new javax.swing.GroupLayout(horizontalSpacer);
        horizontalSpacer.setLayout(horizontalSpacerLayout);
        horizontalSpacerLayout.setHorizontalGroup(
            horizontalSpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        horizontalSpacerLayout.setVerticalGroup(
            horizontalSpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(horizontalSpacer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void pagePrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pagePrevButtonActionPerformed
        if (this.searchResultSupport.getCurrentSearchResults() != null) {
            try {
                displaySearchResults(this.searchResultSupport.decrementPageIdx(), false);
            } catch (IllegalArgumentException | ExecutionException ex) {
                logger.log(Level.WARNING, "Decrementing page index failed", ex);
            }
        } else if (this.pagingSupport != null) {
            setBaseChildFactoryPageIdx(this.pagingSupport.getCurrentPageIdx() - 1);
        }
    }//GEN-LAST:event_pagePrevButtonActionPerformed

    private void pageNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageNextButtonActionPerformed
        if (this.searchResultSupport.getCurrentSearchResults() != null) {
            try {
                displaySearchResults(this.searchResultSupport.incrementPageIdx(), false);
            } catch (IllegalArgumentException | ExecutionException ex) {
                logger.log(Level.WARNING, "Decrementing page index failed", ex);
            }
        } else if (this.pagingSupport != null) {
            setBaseChildFactoryPageIdx(this.pagingSupport.getCurrentPageIdx() + 1);
        }

    }//GEN-LAST:event_pageNextButtonActionPerformed

    private void gotoPageTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoPageTextFieldActionPerformed
        try {
            int parsedIdx = Integer.parseInt(this.gotoPageTextField.getText()) - 1;
            // ensure index is [0, pageNumber)
            if (this.searchResultSupport.getCurrentSearchResults() != null) {
                int pageIdx = Math.max(0, Math.min(this.searchResultSupport.getTotalPages() - 1, parsedIdx));
                displaySearchResults(this.searchResultSupport.updatePageIdx(pageIdx), false);
            } else {
                setBaseChildFactoryPageIdx(parsedIdx);
            }
        } catch (IllegalArgumentException | ExecutionException ex) {
            logger.log(Level.WARNING, "Go to page index failed", ex);
            updatePagingComponents();
        }
    }//GEN-LAST:event_gotoPageTextFieldActionPerformed

    private void setBaseChildFactoryPageIdx(int pageIdx) {
        if (this.pagingSupport != null) {
            int boundedPageIdx = Math.max(0, Math.min(this.pagingSupport.getLastKnownPageCount() - 1, pageIdx));
            int currentTab = this.resultViewerTabs.getSelectedIndex();
            if (currentTab != NO_TAB_SELECTED) {
                setNode(this.currentRootNode, false);
                this.pagingSupport.setCurrentPageIdx(boundedPageIdx);
                updatePagingComponents();
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel gotoPageLabel;
    private javax.swing.JTextField gotoPageTextField;
    private javax.swing.JLabel numberOfChildNodesLabel;
    private javax.swing.JButton pageNextButton;
    private javax.swing.JLabel pageNumLabel;
    private javax.swing.JButton pagePrevButton;
    private javax.swing.JLabel pagesLabel;
    private javax.swing.JTabbedPane resultViewerTabs;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets whether or not this result view panel is the "main" result view
     * panel used to view the child nodes of a node selected in the application
     * tree view (DirectoryTreeTopComponent) that is normally docked into the
     * left hand side of the main window.
     *
     * @return True or false.
     *
     * @deprecated This method has no valid use case.
     */
    @Deprecated
    @Override
    public boolean isMain() {
        return this.isMain;
    }

    /**
     * Sets the label text that displays the number of the child nodes displayed
     * by this result view panel's result viewers.
     *
     * @param numberOfChildNodes The number of child nodes.
     *
     * @deprecated Use setNumberOfChildNodes instead.
     */
    @Deprecated
    public void setNumMatches(Integer numberOfChildNodes) {
        this.setNumberOfChildNodes(numberOfChildNodes);
    }

    /**
     * Resets the state of this results panel.
     *
     * @param unusedSelectedNode Unused.
     *
     * @deprecated Use setNode(null) instead.
     */
    @Deprecated
    public void resetTabs(Node unusedSelectedNode) {
        this.setNode(null);
    }

    /**
     * Displays results of querying the DAO for data artifacts matching the
     * search parameters query.
     *
     * @param dataArtifactParams The search parameter query.
     */
    void displayDataArtifact(DataArtifactSearchParam dataArtifactParams) {
        try {
            SearchResultsDTO results = searchResultSupport.setDataArtifact(dataArtifactParams);
            displaySearchResults(results, true);
        } catch (ExecutionException ex) {
            logger.log(Level.WARNING,
                    MessageFormat.format("There was an error displaying search results for [artifact type: {0}, data source id: {1}]",
                            dataArtifactParams.getArtifactType(),
                            dataArtifactParams.getDataSourceId() == null ? "<null>" : dataArtifactParams.getDataSourceId()),
                    ex);
        }
    }

    void displayAnalysisResult(AnalysisResultSearchParam analysisResultParams) {
        try {
            SearchResultsDTO results = searchResultSupport.setAnalysisResult(analysisResultParams);
            displaySearchResults(results, true);
        } catch (ExecutionException ex) {
            logger.log(Level.WARNING,
                    MessageFormat.format("There was an error displaying search results for [artifact type: {0}, data source id: {1}]",
                            analysisResultParams.getArtifactType(),
                            analysisResultParams.getDataSourceId() == null ? "<null>" : analysisResultParams.getDataSourceId()),
                    ex);
        }
    }

    /**
     * Displays results of querying the DAO for files matching the file
     * extension search parameters query.
     *
     * @param fileExtensionsParams The search parameter query.
     */
    void displayFileExtensions(FileTypeExtensionsSearchParams fileExtensionsParams) {
        try {
            SearchResultsDTO results = searchResultSupport.setFileExtensions(fileExtensionsParams);
            displaySearchResults(results, true);
        } catch (ExecutionException ex) {
            logger.log(Level.WARNING,
                    MessageFormat.format("There was an error displaying search results for [search filter: {0}, data source id: {1}]",
                            fileExtensionsParams.getFilter(),
                            fileExtensionsParams.getDataSourceId() == null ? "<null>" : fileExtensionsParams.getDataSourceId()),
                    ex);
        }
    }

    void displayFileMimes(FileTypeMimeSearchParams fileMimeKey) {
        try {
            SearchResultsDTO results = searchResultSupport.setFileMimes(fileMimeKey);
            displaySearchResults(results, true);
        } catch (ExecutionException | IllegalArgumentException ex) {
            logger.log(Level.WARNING, MessageFormat.format(
                    "There was an error fetching data for files of mime filter: {0} and data source id: {1}.",
                    fileMimeKey.getMimeType(),
                    fileMimeKey.getDataSourceId() == null ? "<null>" : fileMimeKey.getDataSourceId()),
                    ex);
        }

    }

    /**
     * Displays current search result in the result view. This assumes that
     * search result support has already been updated.
     *
     * @param searchResults The new search results to display.
     * @param resetPaging   Whether or not to reset paging to index 0 and tabs
     *                      selection.
     */
    @Messages({
        "# {0} - pageNumber",
        "# {1} - pageCount",
        "DataResultPanel_pageIdxOfCount={0} of {1}"
    })
    private void displaySearchResults(SearchResultsDTO searchResults, boolean resetPaging) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> displaySearchResults(searchResults, resetPaging));
            return;
        }

        if (searchResults == null) {
            setNode(null, resetPaging);
        } else {
            setNode(new SearchResultRootNode(searchResults), resetPaging);
            setNumberOfChildNodes(
                    searchResults.getTotalResultsCount() > Integer.MAX_VALUE
                    ? Integer.MAX_VALUE
                    : (int) searchResults.getTotalResultsCount()
            );
        }
    }

    /**
     * Refreshes the currently displayed search result node by updating the
     * children with the search results as backing data handling errors with a
     * log entry.
     *
     */
    private void refreshSearchResultChildren() {
        try {
            refreshSearchResultChildren(this.searchResultSupport.getRefreshedData());
        } catch (ExecutionException | IllegalArgumentException ex) {
            logger.log(Level.WARNING, "There was an error refreshing data: ", ex);
        }
    }

    /**
     * Refreshes the currently displayed search result node by updating the
     * children with the search results as backing data.
     *
     * @param searchResults The search results to serve as the updated children.
     */
    private void refreshSearchResultChildren(SearchResultsDTO searchResults) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> refreshSearchResultChildren(searchResults));
            return;
        }

        if (searchResults == null) {
            setNode(null, false);
            return;
        }

        SearchResultRootNode searchResultNode = this.currentRootNode instanceof SearchResultRootNode
                ? (SearchResultRootNode) this.currentRootNode
                : null;

        if (searchResultNode == null) {
            displaySearchResults(searchResults, true);
        } else {
            searchResultNode.updateChildren(searchResults);
            setNumberOfChildNodes(
                    searchResults.getTotalResultsCount() > Integer.MAX_VALUE
                    ? Integer.MAX_VALUE
                    : (int) searchResults.getTotalResultsCount()
            );
            updatePagingComponents();
        }
    }

    private void updatePagingComponents() {
        if (this.searchResultSupport.getCurrentSearchResults() != null) {
            this.pagePrevButton.setEnabled(this.searchResultSupport.hasPrevPage());
            this.pageNextButton.setEnabled(this.searchResultSupport.hasNextPage());
            this.pageNumLabel.setText(Bundle.DataResultPanel_pageIdxOfCount(
                    this.searchResultSupport.getPageIdx() + 1,
                    Math.max(this.searchResultSupport.getTotalPages(), 1)));
            this.gotoPageTextField.setText(Integer.toString(this.searchResultSupport.getPageIdx() + 1));
        } else if (this.pagingSupport != null) {
            this.pagePrevButton.setEnabled(this.pagingSupport.getCurrentPageIdx() > 0);
            this.pageNextButton.setEnabled(this.pagingSupport.getCurrentPageIdx() < this.pagingSupport.getLastKnownPageCount() - 1);
            this.pageNumLabel.setText(Bundle.DataResultPanel_pageIdxOfCount(
                    this.pagingSupport.getCurrentPageIdx() + 1,
                    Math.max(this.pagingSupport.getLastKnownPageCount(), 1)));
            this.gotoPageTextField.setText(Integer.toString(this.pagingSupport.getCurrentPageIdx() + 1));
        } else {
            this.pagePrevButton.setEnabled(false);
            this.pageNextButton.setEnabled(false);
            this.pageNumLabel.setText("");
            this.gotoPageTextField.setText("");

        }
    }

    /**
     * Listens for updates in page count for a BaseChildFactory.
     */
    private class BaseChildFactoryPager {

        private final String nodeName;
        private int lastKnownPageCount = 0;
        private int currentPageIdx = 0;

        BaseChildFactoryPager(String nodeName) {
            this.nodeName = nodeName;
        }

        int getLastKnownPageCount() {
            return lastKnownPageCount;
        }

        int getCurrentPageIdx() {
            return currentPageIdx;
        }

        void setCurrentPageIdx(int currentPageIdx) {
            this.currentPageIdx = Math.min(getLastKnownPageCount(), Math.max(0, currentPageIdx));
            postPageChangeEvent();
        }

        /**
         * Notify subscribers (i.e. child factories) that a page change has
         * occurred.
         */
        void postPageChangeEvent() {
            try {
                BaseChildFactory.post(nodeName, new PageChangeEvent(currentPageIdx + 1));
            } catch (BaseChildFactory.NoSuchEventBusException ex) {
                logger.log(Level.WARNING, "Failed to post page change event.", ex); //NON-NLS
            }

            if (pagingSupport == this) {
                updatePagingComponents();
            }
        }

        /**
         * Notify subscribers (i.e. child factories) that a page size change has
         * occurred.
         */
        void postPageSizeChangeEvent() {
            try {
                BaseChildFactory.post(nodeName, new PageSizeChangeEvent(UserPreferences.getResultsTablePageSize()));
                this.currentPageIdx = 0;
            } catch (BaseChildFactory.NoSuchEventBusException ex) {
                logger.log(Level.WARNING, "Failed to post page size change event.", ex); //NON-NLS
            }

            if (pagingSupport == this) {
                updatePagingComponents();
            }
        }

        /**
         * Subscribe to notification that the number of pages has changed.
         *
         * @param event
         */
        @Subscribe
        public void subscribeToPageCountChange(PageCountChangeEvent event) {
            this.lastKnownPageCount = event.getPageCount();
            if (DataResultPanel.this.searchResultSupport.getCurrentSearchResults() == null
                    && event != null
                    && this.nodeName != null
                    && DataResultPanel.this.currentRootNode != null
                    && this.nodeName.equals(DataResultPanel.this.currentRootNode.getName())) {
                this.lastKnownPageCount = event.getPageCount();
                updatePagingComponents();
            }
        }
    }
}
