package com.researchspace.service;

import com.researchspace.fieldmark.model.FieldmarkNotebook;
import com.researchspace.model.User;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

public interface FieldmarkImporterManager {

  List<String> getNotebookList(User user) throws MalformedURLException, URISyntaxException;

  List<FieldmarkNotebook> importNotebook(User user, String notebookId);
}
