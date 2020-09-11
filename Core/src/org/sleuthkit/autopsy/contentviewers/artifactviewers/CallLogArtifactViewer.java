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
package org.sleuthkit.autopsy.contentviewers.artifactviewers;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JScrollPane;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.centralrepository.datamodel.CentralRepository;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.guiutils.ContactCache;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.DataSource;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Artifact viewer for Call log artifacts.
 *
 * Displays the To/From and other parties, and metadata for a call.
 */
@ServiceProvider(service = ArtifactContentViewer.class)
public class CallLogArtifactViewer extends javax.swing.JPanel implements ArtifactContentViewer {

    private final static Logger logger = Logger.getLogger(CallLogArtifactViewer.class.getName());
    private static final long serialVersionUID = 1L;

    private static final Set<Integer> HANDLED_ATTRIBUTE_TYPES = new HashSet<Integer>(Arrays.asList(
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ID.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DIRECTION.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_START.getTypeID(),
            BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_END.getTypeID()
    ));

    private GridBagLayout m_gridBagLayout = new GridBagLayout();
    private GridBagConstraints m_constraints = new GridBagConstraints();

    private PersonaAccountFetcher currentAccountFetcher = null;

    /**
     * Creates new form CallLogArtifactViewer.
     */
    public CallLogArtifactViewer() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void setArtifact(BlackboardArtifact artifact) {
        resetComponent();

        if (artifact == null) {
            return;
        }

        CallLogViewData callLogViewData = null;
        try {
            callLogViewData = getCallLogViewData(artifact);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, String.format("Error getting attributes for Calllog artifact (artifact_id=%d, obj_id=%d)", artifact.getArtifactID(), artifact.getObjectID()), ex);
        }

        // update the view with the call log data
        if (callLogViewData != null) {
            List<AccountPersonaSearcherData> personaSearchDataList = updateView(callLogViewData);
            if (!personaSearchDataList.isEmpty()) {
                currentAccountFetcher = new PersonaAccountFetcher(artifact, personaSearchDataList, this);
                currentAccountFetcher.execute();
            } else {
                currentAccountFetcher = null;
            }
        }
        // repaint
        this.revalidate();
    }

    /**
     * Extracts data from the call log artifact for display in the view.
     *
     * @param artifact Artifact to extract data from.
     *
     * @return CallLogViewData Extracted data to be displayed.
     *
     * @throws TskCoreException
     */
    private CallLogViewData getCallLogViewData(BlackboardArtifact artifact) throws TskCoreException {

        if (artifact == null) {
            return null;
        }

        BlackboardAttribute directionAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DIRECTION));
        BlackboardAttribute toAccountAttr = null;
        BlackboardAttribute fromAccountAttr = null;
        BlackboardAttribute localAccountAttr = null;

        CallLogViewData callLogViewData = null;

        String direction = null;
        String fromAccountIdentifier = null;
        String toAccountIdentifier = null;
        List<String> otherParties = null;
        List<String> toContactNames = null;
        List<String> fromContactNames = null;

        Content dataSource = artifact.getDataSource();
        String deviceId = ((DataSource) dataSource).getDeviceId();

        if (directionAttr != null) {
            direction = directionAttr.getValueString();
            if (direction.equalsIgnoreCase("Incoming")) {
                fromAccountAttr = ObjectUtils.firstNonNull(
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM)),
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER)),
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ID))
                );

                toAccountAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO));
                localAccountAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO));
            } else if (direction.equalsIgnoreCase("Outgoing")) {
                toAccountAttr = ObjectUtils.firstNonNull(
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO)),
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER)),
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ID))
                );

                fromAccountAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM));
                localAccountAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM));
            }
        } else {
            // if direction isn't known, check all the usual attributes that may have the number/address
            // in the absence of sufficent data, any number available will be displayed as a From address.
            if (fromAccountAttr == null) {
                fromAccountAttr = ObjectUtils.firstNonNull(
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM)),
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO)),
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER)),
                        artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ID))
                );
            }
        }

        // get the from account address
        if (fromAccountAttr != null) {
            String fromAccountAttrValue = fromAccountAttr.getValueString();
            if (fromAccountAttrValue.equalsIgnoreCase(deviceId) == false) {
                fromAccountIdentifier = fromAccountAttrValue;
                fromContactNames = ContactCache.getContactNameList(fromAccountIdentifier);
            }
        }

        if (toAccountAttr != null) {
            // TO may be a list of comma separated values.
            String[] numbers = toAccountAttr.getValueString().split(",");
            String toAccountAttrValue = StringUtils.trim(numbers[0]);
            if (toAccountAttrValue.equalsIgnoreCase(deviceId) == false) {
                toAccountIdentifier = toAccountAttrValue;
                toContactNames = ContactCache.getContactNameList(toAccountIdentifier);
            }

            // if more than one To address, then stick the rest of them in the 
            // "Other parties" list.
            if (numbers.length > 1) {
                otherParties = new ArrayList<>();
                for (int i = 1; i < numbers.length; i++) {
                    otherParties.add(StringUtils.trim(numbers[i]));
                }
            }
        }

        // if we have at least one address attribute
        if (null != fromAccountAttr || null != toAccountAttr) {
            callLogViewData = new CallLogViewData(fromAccountIdentifier, toAccountIdentifier);
            callLogViewData.setDirection(direction);

            callLogViewData.setOtherParties(otherParties);

            extractTimeAndDuration(artifact, callLogViewData);

            callLogViewData.setDataSourceName(dataSource.getName());

            // set local account, if it can be deduced.
            if (localAccountAttr != null) {
                String attrValue = localAccountAttr.getValueString();
                // value must be a singular address and not a deviceId to be the local account id
                if (attrValue.equalsIgnoreCase(deviceId) == false && attrValue.contains(",") == false) {
                    callLogViewData.setLocalAccountId(attrValue);
                }
            }

            callLogViewData.setOtherAttributes(extractOtherAttributes(artifact));

            callLogViewData.setFromContactNameList(fromContactNames);
            callLogViewData.setToContactNameList(toContactNames);
        }

        return callLogViewData;
    }

    /**
     * Extract the call time and duration from the artifact and saves in the
     * CallLogViewData.
     *
     * @param artifact        Call log artifact.
     * @param callLogViewData CallLogViewData object to save the time & duration
     *                        in.
     *
     * @throws TskCoreException
     */
    private void extractTimeAndDuration(BlackboardArtifact artifact, CallLogViewData callLogViewData) throws TskCoreException {

        BlackboardAttribute startTimeAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_START));
        if (startTimeAttr == null) {
            startTimeAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME));
        }
        if (startTimeAttr != null) {
            long startTime = startTimeAttr.getValueLong();
            callLogViewData.setDateTimeStr(startTimeAttr.getDisplayString());

            BlackboardAttribute endTimeAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_END));
            if (endTimeAttr != null) {
                long endTime = endTimeAttr.getValueLong();
                if (endTime > 0 && (endTime - startTime) > 0) {
                    callLogViewData.setDuration(String.format("%d seconds", (endTime - startTime)));
                }
            }
        }
    }

    /**
     * Returns the attributes from the given artifact that are not already
     * displayed by the artifact viewer.
     *
     * @param artifact Call log artifact.
     *
     * @return Attribute names/values.
     *
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

    /**
     * Update the viewer with the call log data.
     *
     * @param callLogViewData Call log data to update the view with.
     *
     * @return List of AccountPersonaSearcherData objects.
     */
    @NbBundle.Messages({
        "CallLogArtifactViewer_heading_parties=Parties",
        "CallLogArtifactViewer_value_unknown=Unknown",
        "CallLogArtifactViewer_label_from=From",
        "CallLogArtifactViewer_label_to=To"
    })
    private List<AccountPersonaSearcherData> updateView(CallLogViewData callLogViewData) {

        CommunicationArtifactViewerHelper.addHeader(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_heading_parties());

        List<AccountPersonaSearcherData> dataList = new ArrayList<>();
        // Display "From" if we have non-local device accounts
        if (callLogViewData.getFromAccount() != null) {
            CommunicationArtifactViewerHelper.addKey(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_label_from());
            
            // check if this is local account
            String accountDisplayString = getAccountDisplayString(callLogViewData.getFromAccount(), callLogViewData);
            CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, accountDisplayString);

            List<String> contactNames = callLogViewData.getFromContactNameList();
            for (String name : contactNames) {
                CommunicationArtifactViewerHelper.addContactRow(this, m_gridBagLayout, m_constraints, name);
            }

            // show persona
            dataList.addAll(CommunicationArtifactViewerHelper.addPersonaRow(this, m_gridBagLayout, this.m_constraints, callLogViewData.getFromAccount()));
        }

        // Display "To" if we have non-local device accounts
        if (callLogViewData.getToAccount() != null) {
            CommunicationArtifactViewerHelper.addKey(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_label_to());
            String accountDisplayString = getAccountDisplayString(callLogViewData.getToAccount(), callLogViewData);
            CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, accountDisplayString);

            List<String> contactNames = callLogViewData.getToContactNameList();
            for (String name : contactNames) {
                CommunicationArtifactViewerHelper.addContactRow(this, m_gridBagLayout, m_constraints, name);
            }

            dataList.addAll(CommunicationArtifactViewerHelper.addPersonaRow(this, m_gridBagLayout, this.m_constraints, callLogViewData.getToAccount()));

        }

        // Display other parties
        for (String otherParty : callLogViewData.getOtherParties()) {
            CommunicationArtifactViewerHelper.addKey(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_label_to());
            CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, otherParty);

            dataList.addAll(CommunicationArtifactViewerHelper.addPersonaRow(this, m_gridBagLayout, this.m_constraints, otherParty));
        }

        updateMetadataView(callLogViewData);

        updateOtherAttributesView(callLogViewData);

        updateSourceView(callLogViewData);

        if (CentralRepository.isEnabled() == false) {
            showCRDisabledMessage();
        }

        CommunicationArtifactViewerHelper.addPageEndGlue(this, m_gridBagLayout, this.m_constraints);

        this.setLayout(m_gridBagLayout);
        this.revalidate();
        this.repaint();

        return dataList;
    }

    /**
     * Update the call log meta data section.
     *
     * @param callLogViewData Call log data.
     */
    @NbBundle.Messages({
        "CallLogArtifactViewer_heading_metadata=Metadata",
        "CallLogArtifactViewer_label_direction=Direction",
        "CallLogArtifactViewer_label_date=Date",
        "CallLogArtifactViewer_label_duration=Duration"
    })
    private void updateMetadataView(CallLogViewData callLogViewData) {

        CommunicationArtifactViewerHelper.addHeader(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_heading_metadata());

        CommunicationArtifactViewerHelper.addKey(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_label_direction());
        if (callLogViewData.getDirection() != null) {
            CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, callLogViewData.getDirection());
        } else {
            CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_value_unknown());
        }

        if (callLogViewData.getDateTimeStr() != null) {
            CommunicationArtifactViewerHelper.addKey(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_label_date());
            CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, callLogViewData.getDateTimeStr());
        }

        if (callLogViewData.getDuration() != null) {
            CommunicationArtifactViewerHelper.addKey(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_label_duration());
            CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, callLogViewData.getDuration());
        }

    }

    /**
     * Update the call log source section.
     *
     * @param callLogViewData
     */
    @NbBundle.Messages({
        "CallLogArtifactViewer_heading_Source=Source",
        "CallLogArtifactViewer_label_datasource=Data Source",})
    private void updateSourceView(CallLogViewData callLogViewData) {
        CommunicationArtifactViewerHelper.addHeader(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_heading_Source());
        CommunicationArtifactViewerHelper.addKey(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_label_datasource());
        CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, callLogViewData.getDataSourceName());
    }

    /**
     * Update the other attributes section.
     *
     * @param callLogViewData Call log data.
     */
    @NbBundle.Messages({
        "CallLogArtifactViewer_heading_others=Other Attributes"
    })
    private void updateOtherAttributesView(CallLogViewData callLogViewData) {

        if (callLogViewData.getOtherAttributes().isEmpty()) {
            return;
        }
        CommunicationArtifactViewerHelper.addHeader(this, m_gridBagLayout, this.m_constraints, Bundle.CallLogArtifactViewer_heading_others());

        for (Map.Entry<String, String> entry : callLogViewData.getOtherAttributes().entrySet()) {
            CommunicationArtifactViewerHelper.addKey(this, m_gridBagLayout, this.m_constraints, entry.getKey());
            CommunicationArtifactViewerHelper.addValue(this, m_gridBagLayout, this.m_constraints, entry.getValue());
        }
    }

    @NbBundle.Messages({
        "CalllogArtifactViewer_cr_disabled_message=Enable Central Repository to view, create and edit personas."
    })
    private void showCRDisabledMessage() {
        CommunicationArtifactViewerHelper.addBlankLine(this, m_gridBagLayout, m_constraints);
        m_constraints.gridy++;
        CommunicationArtifactViewerHelper.addMessageRow(this, m_gridBagLayout, m_constraints, Bundle.ContactArtifactViewer_cr_disabled_message());
        m_constraints.gridy++;
    }

    /**
     * Returns display string for a account. Checks if the given account is the
     * local account, if it is known. If it is, it appends a "(Local)" suffix to
     * account display string.
     *
     * @param accountIdentifier  Account identifier to check.
     * @param callLogViewDataNew Call log data which may have the lock account.
     *
     * @return Account string to display.
     */
    @NbBundle.Messages({
        "CallLogArtifactViewer_suffix_local=(Local)",})
    private String getAccountDisplayString(String accountIdentifier, CallLogViewData callLogViewDataNew) {
        String accountDisplayValue = accountIdentifier;
        if (callLogViewDataNew.getLocalAccountId() != null && callLogViewDataNew.getLocalAccountId().equalsIgnoreCase(accountIdentifier)) {
            accountDisplayValue += " " + Bundle.CallLogArtifactViewer_suffix_local();
        }
        return accountDisplayValue;
    }

    @Override
    public Component getComponent() {
        return new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    @Override
    public boolean isSupported(BlackboardArtifact artifact) {

        return (artifact != null)
                && (artifact.getArtifactTypeID() == BlackboardArtifact.ARTIFACT_TYPE.TSK_CALLLOG.getTypeID());
    }

    /**
     * Resets all artifact specific state.
     */
    private void resetComponent() {

        // cancel any outstanding persona searching threads.
        if (currentAccountFetcher != null && !currentAccountFetcher.isDone()) {
            currentAccountFetcher.cancel(true);
            currentAccountFetcher = null;
        }

        // clear the panel 
        this.removeAll();
        this.setLayout(null);

        m_gridBagLayout = new GridBagLayout();
        m_constraints = new GridBagConstraints();

        m_constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        m_constraints.gridy = 0;
        m_constraints.gridx = 0;
        m_constraints.weighty = 0.0;
        m_constraints.weightx = 0.0; // keep components fixed horizontally.
        m_constraints.insets = new java.awt.Insets(0, CommunicationArtifactViewerHelper.LEFT_INSET, 0, 0);
        m_constraints.fill = GridBagConstraints.NONE;

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
