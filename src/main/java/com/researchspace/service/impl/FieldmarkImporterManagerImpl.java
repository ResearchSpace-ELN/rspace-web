package com.researchspace.service.impl;

import static com.researchspace.service.IntegrationsHandler.FIELDMARK_APP_NAME;

import com.researchspace.fieldmark.client.FieldmarkClient;
import com.researchspace.fieldmark.model.FieldmarkNotebook;
import com.researchspace.fieldmark.model.FieldmarkRecordsJsonExport;
import com.researchspace.model.User;
import com.researchspace.model.oauth.UserConnection;
import com.researchspace.service.FieldmarkImporterManager;
import com.researchspace.service.UserConnectionManager;
import com.researchspace.service.inventory.ContainerApiManager;
import com.researchspace.service.inventory.SampleApiManager;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FieldmarkImporterManagerImpl implements FieldmarkImporterManager {

  @Autowired private FieldmarkClient fieldmarkClient;

  @Autowired private SampleApiManager sampleApiMgr;

  @Autowired private ContainerApiManager containerApiMgr;

  @Autowired private UserConnectionManager userConnectionManager;

  @Override
  public List<String> getNotebookList(User user) throws MalformedURLException, URISyntaxException {

    UserConnection existingConnection =
        userConnectionManager
            .findByUserNameProviderName(user.getUsername(), FIELDMARK_APP_NAME)
            .orElseThrow(UnsupportedOperationException::new);

    List<FieldmarkNotebook> fieldmarkNotebooks =
        fieldmarkClient.getNotebooks(existingConnection.getAccessToken());
    return fieldmarkNotebooks.stream()
        .map(nb -> nb.getNonUniqueProjectId())
        .collect(Collectors.toList());
  }

  @Override
  public List<FieldmarkNotebook> importNotebook(User user, String notebookId) {
    UserConnection existingConnection =
        userConnectionManager
            .findByUserNameProviderName(user.getUsername(), FIELDMARK_APP_NAME)
            .orElseThrow(UnsupportedOperationException::new);

    FieldmarkRecordsJsonExport records =
        fieldmarkClient.getNotebookRecords(existingConnection.getAccessToken(), notebookId);


    if(records.hasFiles()){
      String formId = records.getFormId();
      // call CSV
      // call ZIP
    }

    // create the FieldmarkDTO


    return null; // TODO[nik]
  }
}
