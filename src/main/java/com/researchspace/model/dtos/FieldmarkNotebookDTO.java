package com.researchspace.model.dtos;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FieldmarkNotebookDTO {

  private String name;
  private String id;
  private Map<String, String> metadata;

  private List<FielkdmarkRecordDTO> records;
}
