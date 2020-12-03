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
package org.sleuthkit.autopsy.contentviewers;

import java.awt.Component;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE;
import org.sleuthkit.datamodel.DataSource;
import org.sleuthkit.datamodel.Image;
import org.sleuthkit.datamodel.FsContent;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData.TSK_DB_FILES_TYPE_ENUM;

/**
 * Shows file metadata as a list to make it easy to copy and paste. Typically
 * shows the same data that can also be found in the ResultViewer table, just a
 * different order and allows the full path to be visible in the bottom area.
 */
@ServiceProvider(service = DataContentViewer.class, position = 6)
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
public class Metadata extends javax.swing.JPanel implements DataContentViewer {

    private static final Logger LOGGER = Logger.getLogger(Metadata.class.getName());
    
    /**
     * Creates new form Metadata
     */
    public Metadata() {
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

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setPreferredSize(new java.awt.Dimension(100, 52));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(610, 52));

        jTextPane1.setEditable(false);
        jTextPane1.setPreferredSize(new java.awt.Dimension(600, 52));
        jScrollPane2.setViewportView(jTextPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

    private void customizeComponents() {
        /*
         * jTextPane1.setComponentPopupMenu(rightClickMenu); ActionListener
         * actList = new ActionListener(){ @Override public void
         * actionPerformed(ActionEvent e){ JMenuItem jmi = (JMenuItem)
         * e.getSource(); if(jmi.equals(copyMenuItem)) outputViewPane.copy();
         * else if(jmi.equals(selectAllMenuItem)) outputViewPane.selectAll(); }
         * }; copyMenuItem.addActionListener(actList);
         * selectAllMenuItem.addActionListener(actList);
         */

        Utilities.configureTextPaneAsHtml(jTextPane1);
    }

    private void setText(String str) {
        jTextPane1.setText("<html><body>" + str + "</body></html>"); //NON-NLS
    }

    private void startTable(StringBuilder sb) {
        sb.append("<table>"); //NON-NLS
    }

    private void endTable(StringBuilder sb) {
        sb.append("</table>"); //NON-NLS
    }

    private void addRow(StringBuilder sb, String key, String value) {
        sb.append("<tr><td valign=\"top\">"); //NON-NLS
        sb.append(key);
        sb.append("</td><td>"); //NON-NLS
        sb.append(value);
        sb.append("</td></tr>"); //NON-NLS
    }

    @Messages({
        "Metadata.tableRowTitle.mimeType=MIME Type",
        "Metadata.nodeText.truncated=(results truncated)",
        "Metadata.tableRowTitle.sha1=SHA1",
        "Metadata.tableRowTitle.sha256=SHA-256",
        "Metadata.tableRowTitle.imageType=Type",
        "Metadata.tableRowTitle.sectorSize=Sector Size",
        "Metadata.tableRowTitle.timezone=Time Zone",
        "Metadata.tableRowTitle.deviceId=Device ID",
        "Metadata.tableRowTitle.acquisitionDetails=Acquisition Details",
        "Metadata.tableRowTitle.downloadSource=Downloaded From",
        "Metadata.nodeText.unknown=Unknown",
        "Metadata.nodeText.none=None"})
    @Override
    public void setNode(Node node) {
        AbstractFile file = node.getLookup().lookup(AbstractFile.class);
        Image image = node.getLookup().lookup(Image.class);
        DataSource dataSource = node.getLookup().lookup(DataSource.class);
        if (file == null && image == null) {
            setText(NbBundle.getMessage(this.getClass(), "Metadata.nodeText.nonFilePassedIn"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        startTable(sb);

        if (file != null) {
            try {
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.name"), file.getUniquePath());
            } catch (TskCoreException ex) {
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.name"), file.getParentPath() + "/" + file.getName());
            }

            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.type"), file.getType().getName());
            addRow(sb, Bundle.Metadata_tableRowTitle_mimeType(), file.getMIMEType());
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.size"), Long.toString(file.getSize()));
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.fileNameAlloc"), file.getDirFlagAsString());
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.metadataAlloc"), file.getMetaFlagsAsString());
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.modified"), ContentUtils.getStringTime(file.getMtime(), file));
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.accessed"), ContentUtils.getStringTime(file.getAtime(), file));
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.created"), ContentUtils.getStringTime(file.getCrtime(), file));
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.changed"), ContentUtils.getStringTime(file.getCtime(), file));


            String md5 = file.getMd5Hash();
            if (md5 == null) {
                md5 = NbBundle.getMessage(this.getClass(), "Metadata.tableRowContent.md5notCalc");
            }
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.md5"), md5);
            String sha256 = file.getSha256Hash();
            if (sha256 == null) {
                sha256 = NbBundle.getMessage(this.getClass(), "Metadata.tableRowContent.md5notCalc");
            }
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.sha256"), sha256);
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.hashLookupResults"), file.getKnown().toString());
            addAcquisitionDetails(sb, dataSource);
            
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.internalid"), Long.toString(file.getId()));
            if (file.getType().compareTo(TSK_DB_FILES_TYPE_ENUM.LOCAL) == 0) {
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.localPath"), file.getLocalAbsPath());
            }
            
            try {
                List<BlackboardArtifact> associatedObjectArtifacts = file.getArtifacts(ARTIFACT_TYPE.TSK_ASSOCIATED_OBJECT);
                if (!associatedObjectArtifacts.isEmpty()) {
                    BlackboardArtifact artifact = associatedObjectArtifacts.get(0);
                    BlackboardAttribute associatedArtifactAttribute = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ASSOCIATED_ARTIFACT));
                    if (associatedArtifactAttribute != null) {
                        long artifactId = associatedArtifactAttribute.getValueLong();
                        BlackboardArtifact associatedArtifact = artifact.getSleuthkitCase().getBlackboardArtifact(artifactId);
                        addDownloadSourceRow(sb, associatedArtifact);
                    }
                }
            } catch (TskCoreException ex) {
               sb.append(NbBundle.getMessage(this.getClass(), "Metadata.nodeText.exceptionNotice.text")).append(ex.getLocalizedMessage());
            }
            
            endTable(sb);

            /*
             * If we have a file system file, grab the more detailed metadata text
             * too
             */
            try {
                if (file instanceof FsContent) {
                    FsContent fsFile = (FsContent) file;

                    sb.append("<hr /><pre>\n"); //NON-NLS
                    sb.append(NbBundle.getMessage(this.getClass(), "Metadata.nodeText.text"));
                    sb.append(" <br /><br />"); // NON-NLS
                    for (String str : fsFile.getMetaDataText()) {
                        sb.append(str).append("<br />"); //NON-NLS

                        /* 
                         * Very long results can cause the UI to hang before displaying,
                         * so truncate the results if necessary.
                         */
                        if(sb.length() > 50000){
                            sb.append(NbBundle.getMessage(this.getClass(), "Metadata.nodeText.truncated"));
                            break;
                        }
                    }
                    sb.append("</pre>\n"); //NON-NLS
                }
            } catch (TskCoreException ex) {
                sb.append(NbBundle.getMessage(this.getClass(), "Metadata.nodeText.exceptionNotice.text")).append(ex.getLocalizedMessage());
            }
        } else {
            try {
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.name"), image.getUniquePath());
            } catch (TskCoreException ex) {
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.name"), image.getName());
            }
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.imageType"), image.getType().getName());        
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.size"), Long.toString(image.getSize()));

            try {
                String md5 = image.getMd5();
                if (md5 == null || md5.isEmpty()) {
                    md5 = NbBundle.getMessage(this.getClass(), "Metadata.tableRowContent.md5notCalc");
                }
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.md5"), md5);

                String sha1 = image.getSha1();
                if (sha1 == null || sha1.isEmpty()) {
                    sha1 = NbBundle.getMessage(this.getClass(), "Metadata.tableRowContent.md5notCalc");
                }
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.sha1"), sha1);

                String sha256 = image.getSha256();
                if (sha256 == null || sha256.isEmpty()) {
                    sha256 = NbBundle.getMessage(this.getClass(), "Metadata.tableRowContent.md5notCalc");
                }
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.sha256"), sha256);
            } catch (TskCoreException ex) {
                sb.append(NbBundle.getMessage(this.getClass(), "Metadata.nodeText.exceptionNotice.text")).append(ex.getLocalizedMessage());
            }
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.sectorSize"), Long.toString(image.getSsize()));
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.timezone"), image.getTimeZone());
            addAcquisitionDetails(sb, dataSource);
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.deviceId"), image.getDeviceId());
            addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.internalid"), Long.toString(image.getId()));

            // Add all the data source paths to the "Local Path" value cell.
            String[] imagePaths = image.getPaths();
            if (imagePaths.length > 0) {
                StringBuilder pathValues = new StringBuilder("<div>");
                pathValues.append(imagePaths[0]);
                pathValues.append("</div>");
                for (int i=1; i < imagePaths.length; i++) {
                    pathValues.append("<div>");
                    pathValues.append(imagePaths[i]);
                    pathValues.append("</div>");
                }
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.localPath"), pathValues.toString());
            } else {
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.localPath"), 
                        NbBundle.getMessage(this.getClass(), "Metadata.nodeText.none"));
            }
        }
        
        setText(sb.toString());
        jTextPane1.setCaretPosition(0);
        this.setCursor(null);
    }
    
    /**
     * Adds a row for download source from the given associated artifact, 
     * if the associated artifacts specifies a source.
     * 
     * @param sb    string builder.
     * @param associatedArtifact
     * 
     * @throws TskCoreException if there is an error
     */
    private void addDownloadSourceRow(StringBuilder sb, BlackboardArtifact associatedArtifact ) throws TskCoreException {
        if (associatedArtifact != null && 
                ((associatedArtifact.getArtifactTypeID() == ARTIFACT_TYPE.TSK_WEB_DOWNLOAD.getTypeID()) || 
                 (associatedArtifact.getArtifactTypeID() == ARTIFACT_TYPE.TSK_WEB_CACHE.getTypeID())) ) {
            BlackboardAttribute urlAttr = associatedArtifact.getAttribute(new BlackboardAttribute.Type(ATTRIBUTE_TYPE.TSK_URL));
            if (urlAttr != null) {
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.downloadSource"), urlAttr.getValueString());
            }
        }
    }
    
    /**
     * Add the acquisition details to the results (if applicable)
     * 
     * @param sb         The output StringBuilder object
     * @param dataSource The data source (may be null)
     */
    private void addAcquisitionDetails(StringBuilder sb, DataSource dataSource) {
        if (dataSource != null) {
            try {
                String details = dataSource.getAcquisitionDetails();
                if (StringUtils.isEmpty(details)) {
                    details = Bundle.Metadata_nodeText_unknown();
                }
                details = details.replaceAll("\n", "<br>");
                addRow(sb, NbBundle.getMessage(this.getClass(), "Metadata.tableRowTitle.acquisitionDetails"), details);
            } catch (TskCoreException ex) {
                LOGGER.log(Level.SEVERE, "Error reading acquisition details from case database", ex); //NON-NLS
            }
        }
    }

    @Override
    public String getTitle() {
        return NbBundle.getMessage(this.getClass(), "Metadata.title");
    }

    @Override
    public String getToolTip() {
        return NbBundle.getMessage(this.getClass(), "Metadata.toolTip");
    }

    @Override
    public DataContentViewer createInstance() {
        return new Metadata();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void resetComponent() {
        setText("");
    }

    @Override
    public boolean isSupported(Node node) {
        Image image = node.getLookup().lookup(Image.class);
        AbstractFile file = node.getLookup().lookup(AbstractFile.class);
        return (file != null) || (image != null);
    }

    @Override
    public int isPreferred(Node node) {
        return 1;
    }
}
