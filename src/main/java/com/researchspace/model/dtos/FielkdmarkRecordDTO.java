package com.researchspace.model.dtos;

import com.researchspace.fieldmark.model.utils.FieldmarkTypeExtractor;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FielkdmarkRecordDTO {

  Map<String, FieldmarkTypeExtractor> fields;
}
