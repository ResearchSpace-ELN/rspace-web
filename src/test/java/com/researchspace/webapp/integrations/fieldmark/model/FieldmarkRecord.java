package com.researchspace.webapp.integrations.fieldmark.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldmarkRecord {

  @JsonProperty("project_id")
  public String projectId;

  @JsonProperty("record_id")
  public String recordId;

  @JsonProperty("revision_id")
  public String revisionId;

  public String type;
  public Map<String, Object> data;

  @JsonProperty("updated_by")
  public String updatedBy;

  public Date updated;
  public Date created;

  @JsonProperty("created_by")
  public String createdBy;

  @JsonProperty("field_types")
  public Map<String, String> fieldTypes;

  public boolean deleted;
}
