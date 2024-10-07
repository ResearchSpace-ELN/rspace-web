package com.researchspace.webapp.integrations.fieldmark;

import com.researchspace.service.FieldmarkManager;
import com.researchspace.service.UserConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/apps/fieldmark")
public class FieldmarkController {

  @Value("${fieldmark.api.url}")
  private String webBaseUrl;

  private @Autowired UserConnectionManager userConnectionManager;

  private @Autowired FieldmarkManager fieldmarkManager;
}
