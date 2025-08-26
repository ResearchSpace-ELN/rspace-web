package com.researchspace.webapp.integrations.dmponline;

import com.researchspace.dmptool.model.DMPPlanScope;
import com.researchspace.model.User;
import com.researchspace.model.dmps.DMPUser;
import com.researchspace.model.views.ServiceOperationResult;
import com.researchspace.rda.model.DMP;
import com.researchspace.rda.model.DMPList;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/** API Client wrapper for making calls to DMP API */
public interface DMPOnlineProvider {

  ServiceOperationResult<DMPUser> doJsonDownload(DMP dmp, String title, User user)
      throws URISyntaxException, IOException;

  ServiceOperationResult<DMPList> listPlans(DMPPlanScope scope, User user)
      throws MalformedURLException, URISyntaxException;

  ServiceOperationResult<DMP> getPlanById(String dmpId, User user)
      throws MalformedURLException, URISyntaxException;
}
