/*
 * Central Repository
 *
 * Copyright 2021 Basis Technology Corp.
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
package org.sleuthkit.autopsy.centralrepository.contentviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import org.openide.nodes.Node;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.centralrepository.application.NodeData;
import org.sleuthkit.autopsy.centralrepository.application.OtherOccurrences;
import org.sleuthkit.autopsy.centralrepository.contentviewer.OtherOccurrencesNodeWorker.OtherOccurrencesData;
import org.sleuthkit.autopsy.centralrepository.datamodel.CentralRepoException;
import org.sleuthkit.autopsy.centralrepository.datamodel.CorrelationAttributeInstance;
import org.sleuthkit.autopsy.centralrepository.datamodel.CorrelationCase;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.OsAccount;
import org.sleuthkit.datamodel.TskException;

/**
 * A SwingWorker that gathers data for the OtherOccurencesPanel which appears in
 * the dataContentViewerOtherCases panel.
 */
class OtherOccurrencesNodeWorker extends SwingWorker<OtherOccurrencesData, Void> {

    private static final Logger logger = Logger.getLogger(OtherOccurrencesNodeWorker.class.getName());

    private final Node node;

    /**
     * Constructs a new instance for the given node.
     *
     * @param node
     */
    OtherOccurrencesNodeWorker(Node node) {
        this.node = node;
    }

    @Override
    protected OtherOccurrencesData doInBackground() throws Exception {
        OsAccount osAccount = node.getLookup().lookup(OsAccount.class);
        AbstractFile file = OtherOccurrences.getAbstractFileFromNode(node);
        if (osAccount != null) {
            file = node.getLookup().lookup(AbstractFile.class);
        }
        String deviceId = "";
        String dataSourceName = "";
        Map<String, CorrelationCase> caseNames = new HashMap<>();
        Case currentCase = Case.getCurrentCaseThrows();
        OtherOccurrencesData data = null;
        try {
            if (file != null) {
                Content dataSource = file.getDataSource();
                deviceId = currentCase.getSleuthkitCase().getDataSource(dataSource.getId()).getDeviceId();
                dataSourceName = dataSource.getName();
            }
        } catch (TskException ex) {
            // do nothing. 
            // @@@ Review this behavior
            return null;
        }
        Collection<CorrelationAttributeInstance> correlationAttributes = new ArrayList<>();
        if (osAccount != null) {
            correlationAttributes = OtherOccurrences.getCorrelationAttributeFromOsAccount(node, osAccount);    
        } else {
            correlationAttributes = OtherOccurrences.getCorrelationAttributesFromNode(node, file);
        }
        int totalCount = 0;
        Set<String> dataSources = new HashSet<>();
        for (CorrelationAttributeInstance corAttr : correlationAttributes) {
            for (NodeData nodeData : OtherOccurrences.getCorrelatedInstances(file, deviceId, dataSourceName, corAttr).values()) {
                if (nodeData.isCentralRepoNode()) {
                    try {
                        dataSources.add(OtherOccurrences.makeDataSourceString(nodeData.getCorrelationAttributeInstance().getCorrelationCase().getCaseUUID(), nodeData.getDeviceID(), nodeData.getDataSourceName()));
                        caseNames.put(nodeData.getCorrelationAttributeInstance().getCorrelationCase().getCaseUUID(), nodeData.getCorrelationAttributeInstance().getCorrelationCase());
                    } catch (CentralRepoException ex) {
                        logger.log(Level.WARNING, "Unable to get correlation case for displaying other occurrence for case: " + nodeData.getCaseName(), ex);
                    }
                } else {
                    try {
                        dataSources.add(OtherOccurrences.makeDataSourceString(Case.getCurrentCaseThrows().getName(), nodeData.getDeviceID(), nodeData.getDataSourceName()));
                        caseNames.put(Case.getCurrentCaseThrows().getName(), new CorrelationCase(Case.getCurrentCaseThrows().getName(), Case.getCurrentCaseThrows().getDisplayName()));
                    } catch (NoCurrentCaseException ex) {
                        logger.log(Level.WARNING, "No current case open for other occurrences", ex);
                    }
                }
                totalCount++;

                if (isCancelled()) {
                    break;
                }
            }
        }

        if (!isCancelled()) {
            data = new OtherOccurrencesData(correlationAttributes, file, dataSourceName, deviceId, caseNames, totalCount, dataSources.size(), OtherOccurrences.getEarliestCaseDate());
        }

        return data;
    }

    /**
     * Object to store all of the data gathered in the OtherOccurrencesWorker
     * doInBackground method.
     */
    static class OtherOccurrencesData {

        private final String deviceId;
        private final AbstractFile file;
        private final String dataSourceName;
        private final Map<String, CorrelationCase> caseMap;
        private final int instanceDataCount;
        private final int dataSourceCount;
        private final String earliestCaseDate;
        private final Collection<CorrelationAttributeInstance> correlationAttributes;

        private OtherOccurrencesData(Collection<CorrelationAttributeInstance> correlationAttributes, AbstractFile file, String dataSourceName, String deviceId, Map<String, CorrelationCase> caseMap, int instanceCount, int dataSourceCount, String earliestCaseDate) {
            this.file = file;
            this.deviceId = deviceId;
            this.dataSourceName = dataSourceName;
            this.caseMap = caseMap;
            this.instanceDataCount = instanceCount;
            this.dataSourceCount = dataSourceCount;
            this.earliestCaseDate = earliestCaseDate;
            this.correlationAttributes = correlationAttributes;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public AbstractFile getFile() {
            return file;
        }

        public String getDataSourceName() {
            return dataSourceName;
        }

        public Map<String, CorrelationCase> getCaseMap() {
            return caseMap;
        }

        public int getInstanceDataCount() {
            return instanceDataCount;
        }

        public int getDataSourceCount() {
            return dataSourceCount;
        }

        /**
         * Returns the earliest date in the case.
         *
         * @return Formatted date string, or message that one was not found.
         */
        public String getEarliestCaseDate() {
            return earliestCaseDate;
        }

        public Collection<CorrelationAttributeInstance> getCorrelationAttributes() {
            return correlationAttributes;
        }
    }
}
