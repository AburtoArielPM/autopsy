/*
 * Autopsy Forensic Browser
 *
 * Copyright 2020 Basis Technology Corp.
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
package org.sleuthkit.autopsy.contentviewers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JScrollPane;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.DataSource;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * This is a viewer for TSK_CALLLOG artifacts.
 *
 *
 */
public class CallLogArtifactViewer extends javax.swing.JPanel implements ArtifactContentViewer {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private final static Logger logger = Logger.getLogger(CallLogArtifactViewer.class.getName());
    private static final long serialVersionUID = 1L;

    private static final Set<Integer> HANDLED_ATTRIBUTE_TYPES = new HashSet<Integer>(Arrays.asList(
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DIRECTION.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_START.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_END.getTypeID()
    ));

    /**
     * Creates new form CalllogArtifactViewer
     */
    public CallLogArtifactViewer() {
        initComponents();
        customizeComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        callDetailsPanel = new javax.swing.JPanel();
        toOrFromNumberLabel = new javax.swing.JLabel();
        personaButton1 = new javax.swing.JButton();
        toOrFromNameLabel = new javax.swing.JLabel();
        directionLabel = new javax.swing.JLabel();
        dateTimeLabel = new javax.swing.JLabel();
        durationLabel = new javax.swing.JLabel();
        otherParticipantsPanel = new javax.swing.JPanel();
        otherParticipantsListPanel = new javax.swing.JPanel();
        otherParticipantsLabel = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        otherAttributesPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        otherAttributesListPanel = new javax.swing.JPanel();
        localAccountInfoPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        dataSourceNameLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        deviceIdLabel = new javax.swing.JLabel();
        localAccountLabel = new javax.swing.JLabel();
        localAccountIdLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        callDetailsPanel.setPreferredSize(new java.awt.Dimension(400, 150));

        toOrFromNumberLabel.setFont(toOrFromNumberLabel.getFont().deriveFont(toOrFromNumberLabel.getFont().getSize()+2f));
        org.openide.awt.Mnemonics.setLocalizedText(toOrFromNumberLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.toOrFromNumberLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(personaButton1, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.personaButton1.text")); // NOI18N
        personaButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                personaButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(toOrFromNameLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.toOrFromNameLabel.text")); // NOI18N
        toOrFromNameLabel.setEnabled(false);

        directionLabel.setFont(directionLabel.getFont());
        org.openide.awt.Mnemonics.setLocalizedText(directionLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.directionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dateTimeLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.dateTimeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(durationLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.durationLabel.text")); // NOI18N

        javax.swing.GroupLayout callDetailsPanelLayout = new javax.swing.GroupLayout(callDetailsPanel);
        callDetailsPanel.setLayout(callDetailsPanelLayout);
        callDetailsPanelLayout.setHorizontalGroup(
            callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(callDetailsPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(directionLabel)
                    .addGroup(callDetailsPanelLayout.createSequentialGroup()
                        .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(toOrFromNumberLabel)
                            .addGroup(callDetailsPanelLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(toOrFromNameLabel)))
                        .addGap(18, 18, 18)
                        .addComponent(personaButton1))
                    .addGroup(callDetailsPanelLayout.createSequentialGroup()
                        .addComponent(dateTimeLabel)
                        .addGap(18, 18, 18)
                        .addComponent(durationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(211, Short.MAX_VALUE))
        );
        callDetailsPanelLayout.setVerticalGroup(
            callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(callDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(callDetailsPanelLayout.createSequentialGroup()
                        .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(toOrFromNumberLabel)
                            .addComponent(personaButton1))
                        .addGap(28, 28, 28))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, callDetailsPanelLayout.createSequentialGroup()
                        .addComponent(toOrFromNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateTimeLabel)
                    .addComponent(durationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(directionLabel)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        add(callDetailsPanel, java.awt.BorderLayout.PAGE_START);

        javax.swing.GroupLayout otherParticipantsListPanelLayout = new javax.swing.GroupLayout(otherParticipantsListPanel);
        otherParticipantsListPanel.setLayout(otherParticipantsListPanelLayout);
        otherParticipantsListPanelLayout.setHorizontalGroup(
            otherParticipantsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 406, Short.MAX_VALUE)
        );
        otherParticipantsListPanelLayout.setVerticalGroup(
            otherParticipantsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 168, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(otherParticipantsLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.otherParticipantsLabel.text")); // NOI18N

        javax.swing.GroupLayout otherParticipantsPanelLayout = new javax.swing.GroupLayout(otherParticipantsPanel);
        otherParticipantsPanel.setLayout(otherParticipantsPanelLayout);
        otherParticipantsPanelLayout.setHorizontalGroup(
            otherParticipantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherParticipantsPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(otherParticipantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(otherParticipantsListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(otherParticipantsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        otherParticipantsPanelLayout.setVerticalGroup(
            otherParticipantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherParticipantsPanelLayout.createSequentialGroup()
                .addComponent(otherParticipantsLabel)
                .addGap(0, 0, 0)
                .addComponent(otherParticipantsListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(otherParticipantsPanel, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout otherAttributesListPanelLayout = new javax.swing.GroupLayout(otherAttributesListPanel);
        otherAttributesListPanel.setLayout(otherAttributesListPanelLayout);
        otherAttributesListPanelLayout.setHorizontalGroup(
            otherAttributesListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 413, Short.MAX_VALUE)
        );
        otherAttributesListPanelLayout.setVerticalGroup(
            otherAttributesListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 88, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout otherAttributesPanelLayout = new javax.swing.GroupLayout(otherAttributesPanel);
        otherAttributesPanel.setLayout(otherAttributesPanelLayout);
        otherAttributesPanelLayout.setHorizontalGroup(
            otherAttributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherAttributesPanelLayout.createSequentialGroup()
                .addGroup(otherAttributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(otherAttributesListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(otherAttributesPanelLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24))
        );
        otherAttributesPanelLayout.setVerticalGroup(
            otherAttributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherAttributesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(otherAttributesListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        bottomPanel.add(otherAttributesPanel, java.awt.BorderLayout.PAGE_START);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dataSourceNameLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.dataSourceNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deviceIdLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.deviceIdLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(localAccountLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.localAccountLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(localAccountIdLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.localAccountIdLabel.text")); // NOI18N

        javax.swing.GroupLayout localAccountInfoPanelLayout = new javax.swing.GroupLayout(localAccountInfoPanel);
        localAccountInfoPanel.setLayout(localAccountInfoPanelLayout);
        localAccountInfoPanelLayout.setHorizontalGroup(
            localAccountInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(localAccountInfoPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(localAccountInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2)
                    .addComponent(localAccountLabel))
                .addGap(18, 18, 18)
                .addGroup(localAccountInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(localAccountIdLabel)
                    .addGroup(localAccountInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(dataSourceNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                        .addComponent(deviceIdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(158, Short.MAX_VALUE))
        );
        localAccountInfoPanelLayout.setVerticalGroup(
            localAccountInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(localAccountInfoPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(localAccountInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localAccountLabel)
                    .addComponent(localAccountIdLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(localAccountInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dataSourceNameLabel)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(localAccountInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(deviceIdLabel))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        bottomPanel.add(localAccountInfoPanel, java.awt.BorderLayout.PAGE_END);

        add(bottomPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void personaButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_personaButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_personaButton1ActionPerformed

    private void customizeComponents() {
        // disable the name label for now.
        this.toOrFromNameLabel.setVisible(false);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel callDetailsPanel;
    private javax.swing.JLabel dataSourceNameLabel;
    private javax.swing.JLabel dateTimeLabel;
    private javax.swing.JLabel deviceIdLabel;
    private javax.swing.JLabel directionLabel;
    private javax.swing.JLabel durationLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel localAccountIdLabel;
    private javax.swing.JPanel localAccountInfoPanel;
    private javax.swing.JLabel localAccountLabel;
    private javax.swing.JPanel otherAttributesListPanel;
    private javax.swing.JPanel otherAttributesPanel;
    private javax.swing.JLabel otherParticipantsLabel;
    private javax.swing.JPanel otherParticipantsListPanel;
    private javax.swing.JPanel otherParticipantsPanel;
    private javax.swing.JButton personaButton1;
    private javax.swing.JLabel toOrFromNameLabel;
    private javax.swing.JLabel toOrFromNumberLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Encapsulates the information to be displayed about the call log artifact.
     */
    private class CallLogViewData {

        // primary to/from number/adddress/accountId
        String number;
        String name = null;
        String direction;
        String dateTimeStr = null;
        String duration = null;
        Collection<String> otherParticipants = new ArrayList<>();
        String dataSourceName = null;
        String dataSourceDeviceId = null;
        String localAccountId = null; // number/accountId of device owner, may not be always known
        Map<String, String> otherAttributes = new HashMap<>();

        CallLogViewData(String number) {
            this(number, null);
        }

        CallLogViewData(String number, String direction) {
            this.number = number;
            this.direction = direction;
        }

        String getNumber() {
            return number;
        }

        void setNumber(String number) {
            this.number = number;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getDirection() {
            return direction;
        }

        void setDirection(String direction) {
            this.direction = direction;
        }

        String getDataSourceName() {
            return dataSourceName;
        }

        void setDataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
        }

        String getDataSourceDeviceId() {
            return dataSourceDeviceId;
        }

        void setDataSourceDeviceId(String dataSourceDeviceId) {
            this.dataSourceDeviceId = dataSourceDeviceId;
        }

        String getDateTimeStr() {
            return dateTimeStr;
        }

        void setDateTimeStr(String dateTimeStr) {
            this.dateTimeStr = dateTimeStr;
        }

        String getDuration() {
            return duration;
        }

        void setDuration(String duration) {
            this.duration = duration;
        }

        Collection<String> getOtherParticipants() {
            return Collections.unmodifiableCollection(otherParticipants);
        }

        void setOtherParticipants(Collection<String> otherParticipants) {
            if (otherParticipants != null) {
                this.otherParticipants = new ArrayList<>(otherParticipants);
            }
        }

        public Map<String, String> getOtherAttributes() {
            return Collections.unmodifiableMap(otherAttributes);
        }

        public void setOtherAttributes(Map<String, String> otherAttributes) {
            if (otherParticipants != null) {
                this.otherAttributes = new HashMap<>(otherAttributes);
            }
        }

        public String getLocalAccountId() {
            return localAccountId;
        }

        public void setLocalAccountId(String localAccountId) {
            this.localAccountId = localAccountId;
        }

    }

    @Override
    public void setArtifact(BlackboardArtifact artifact) {

        this.removeAll();
        this.initComponents();
        this.customizeComponents();

        CallLogViewData callLogViewData = null;

        try {
            callLogViewData = getCallLogViewData(artifact);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, String.format("Error getting attributes for Calllog artifact (artifact_id=%d, obj_id=%d)", artifact.getArtifactID(), artifact.getObjectID()), ex);
        }

        if (callLogViewData != null) {

            this.toOrFromNumberLabel.setText(callLogViewData.getNumber());

            // TBD: Vik-6383 find and display the persona for this account, and a button
            this.personaButton1.setVisible(false);

            if (callLogViewData.getDirection() != null) {
                this.directionLabel.setText(callLogViewData.getDirection());
            } else {
                this.directionLabel.setVisible(false);
            }

            if (callLogViewData.getDateTimeStr() != null) {
                this.dateTimeLabel.setText(callLogViewData.getDateTimeStr());
                if (callLogViewData.getDuration() != null) {
                    this.durationLabel.setText(callLogViewData.getDuration());
                } else {
                    this.durationLabel.setVisible(false);
                }
            } else {
                this.dateTimeLabel.setVisible(false);
                this.durationLabel.setVisible(false);
            }

            // Populate other participants
            updateOtherParticipantsPanel(callLogViewData.getOtherParticipants());

            // Populate other attributs panel
            updateOtherAttributesPanel(callLogViewData.getOtherAttributes());

            // populate local account and data source
            if (callLogViewData.getLocalAccountId() != null) {
                // Vik-6383 find and display the persona for this account, and a button
                this.localAccountIdLabel.setText(callLogViewData.getLocalAccountId());
            } else {
                this.localAccountLabel.setVisible(false);
                this.localAccountIdLabel.setVisible(false);
            }
            if (callLogViewData.getDataSourceName() != null) {
                this.dataSourceNameLabel.setText(callLogViewData.getDataSourceName());
            }
            if (callLogViewData.getDataSourceDeviceId() != null) {
                this.deviceIdLabel.setText(callLogViewData.getDataSourceDeviceId());
            }
        }

        // repaint
        this.revalidate();
    }

    private void updateOtherParticipantsPanel(Collection<String> otherParticipants) {

        if (otherParticipants == null || otherParticipants.isEmpty()) {
            otherParticipantsPanel.setVisible(false);
            return;
        }

        // create a gridbag layout to show each participant on one line
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridy = 0;
        constraints.insets = new java.awt.Insets(4, 12, 0, 0);
        for (String participant : otherParticipants) {
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = 0;
            constraints.gridx = 0;

            javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new Dimension(25, 0), new Dimension(25, 0), new Dimension(25, 0));
            otherParticipantsListPanel.add(filler1, constraints);

            // Add a label partcipant's number/Id
            javax.swing.JLabel participantNumberLabel = new javax.swing.JLabel();
            participantNumberLabel.setText(participant);

            gridBagLayout.setConstraints(participantNumberLabel, constraints);
            otherParticipantsListPanel.add(participantNumberLabel);

            // TBD Vik-6383 find and display the persona for this account, and a button
//            constraints.gridx += 2;
//            javax.swing.JButton personaButton = new javax.swing.JButton();
//            personaButton.setText("Persona");
//            gridBagLayout.setConstraints(personaButton, constraints);
//            otherParticipantsListPanel.add(personaButton);
            // add a filler to take up rest of the space
            constraints.gridx++;
            constraints.weightx = 1.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            otherParticipantsListPanel.add(new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0)));

            constraints.gridy++;
        }
        otherParticipantsListPanel.setLayout(gridBagLayout);

        otherParticipantsPanel.revalidate();

    }

    private void updateOtherAttributesPanel(Map<String, String> otherAttributes) {
        if (otherAttributes == null || otherAttributes.isEmpty()) {
            this.otherAttributesPanel.setVisible(false);
            return;
        }

        // create a gridbag layout to show attribute on one line
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridy = 0;
        constraints.insets = new java.awt.Insets(4, 12, 0, 0);
        for (Map.Entry<String, String> attribute : otherAttributes.entrySet()) {
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = 0;
            constraints.gridx = 0;

            // Add a small horizontal filler at the beginning
            javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new Dimension(25, 0), new Dimension(25, 0), new Dimension(25, 0));
            otherAttributesListPanel.add(filler1, constraints);

            // Add attribute name label
            constraints.gridx++;
            javax.swing.JLabel attrNameLabel = new javax.swing.JLabel();
            attrNameLabel.setText(attribute.getKey());

            gridBagLayout.setConstraints(attrNameLabel, constraints);
            this.otherAttributesListPanel.add(attrNameLabel);

            // Add value
            constraints.gridx += 2;
            javax.swing.JLabel attrValueLabel = new javax.swing.JLabel();
            attrValueLabel.setText(attribute.getValue());

            gridBagLayout.setConstraints(attrValueLabel, constraints);
            this.otherAttributesListPanel.add(attrValueLabel);

            // add a filler to take up rest of the horizontal space
            constraints.gridx++;
            constraints.weightx = 1.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            otherAttributesListPanel.add(new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0)));

            constraints.gridy++;
        }
        otherAttributesListPanel.setLayout(gridBagLayout);
        otherAttributesListPanel.revalidate();
    }

    @Override
    public Component getComponent() {
        return new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    @Override
    public boolean isSupported(BlackboardArtifact artifact) {
        return artifact.getArtifactTypeID() == BlackboardArtifact.ARTIFACT_TYPE.TSK_CALLLOG.getTypeID();
    }

    /**
     * Extracts data from the call log artifact for display in the view.
     *
     * @param artifact Artifact to extract data from.
     * @return CallLogViewData
     * @throws TskCoreException
     */
    private CallLogViewData getCallLogViewData(BlackboardArtifact artifact) throws TskCoreException {

        BlackboardAttribute directionAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DIRECTION));
        BlackboardAttribute numberAttr = null;
        BlackboardAttribute localAccountAttr = null;

        CallLogViewData callLogViewData = null;
        String direction = null;

        if (directionAttr != null) {
            // if direction is known,  depending on the direction,
            // the TO or the FROM attribute is the primary number of interest.
            // annd the other is is possibly the number/address of device owner.
            direction = directionAttr.getValueString();

            if (direction.equalsIgnoreCase("Incoming")) {
                numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM));
                localAccountAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO));
            } else if (direction.equalsIgnoreCase("Outgoing")) {
                numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO));
                localAccountAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM));
            }

        }
        // if direction isn't known, look for any attribute that may have the number/address
        if (numberAttr == null) {
            numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM));
        }

        if (numberAttr == null) {
            numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO));
        }

        if (numberAttr == null) {
            numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER));
        }

        if (numberAttr == null) {
            numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ID));
        }
        
        if (numberAttr != null) {

            // check if it's a list of numbers and and if so,
            // split it, take the first one. and put the others in otherParticicpants....
            String[] numbers = numberAttr.getValueString().split(",");
            List<String> otherNumbers = null;
            if (numbers.length > 1) {
                otherNumbers = new ArrayList<>();
                for (int i = 1; i < numbers.length; i++) {
                    otherNumbers.add(numbers[i]);
                }

            }
            callLogViewData = new CallLogViewData(numbers[0], direction);
            callLogViewData.setOtherParticipants(otherNumbers);

            // get date, duration,
            BlackboardAttribute startTimeAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_START));
            if (startTimeAttr == null) {
                startTimeAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME));
            }
            if (startTimeAttr != null) {
                long startTime = startTimeAttr.getValueLong();
                Content content = artifact.getDataSource();
                if (null != content && 0 != startTime) {
                    dateFormatter.setTimeZone(ContentUtils.getTimeZone(content));
                    callLogViewData.setDateTimeStr(dateFormatter.format(new java.util.Date(startTime * 1000)));
                }

                BlackboardAttribute endTimeAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_END));
                if (endTimeAttr != null) {
                    long endTime = endTimeAttr.getValueLong();
                    if (endTime > 0) {
                        callLogViewData.setDuration(String.format("%d seconds", (endTime - startTime)));
                    }
                }
            }

            Content dataSource = artifact.getDataSource();

            callLogViewData.setDataSourceName(dataSource.getName());
            String deviceId = ((DataSource) dataSource).getDeviceId();
            callLogViewData.setDataSourceDeviceId(deviceId);

            if (localAccountAttr != null) {
                String attrValue = localAccountAttr.getValueString();
                if (attrValue.equalsIgnoreCase(deviceId) == false && attrValue.contains(",") == false) {
                    callLogViewData.setLocalAccountId(attrValue);
                }
            }

            callLogViewData.setOtherAttributes(extractOtherAttributes(artifact));

        }

        return callLogViewData;
    }
 
    /**
     * Returns a map of attribute name/value pairs
     *
     * @param artifact
     * @return
     * @throws TskCoreException
     */
    private Map<String, String> extractOtherAttributes(BlackboardArtifact artifact) throws TskCoreException {
        List<BlackboardAttribute> attributes = artifact.getAttributes();
        Map<String, String> otherAttributes = new HashMap<>();

        for (BlackboardAttribute attr : attributes) {
            if (HANDLED_ATTRIBUTE_TYPES.contains(attr.getAttributeType().getTypeID()) == false) {
                otherAttributes.put(attr.getAttributeType().getDisplayName(), attr.getDisplayString());
            }
        }

        return otherAttributes;
    }
    
}
