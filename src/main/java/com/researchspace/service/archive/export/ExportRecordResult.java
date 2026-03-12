package com.researchspace.service.archive.export;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Data
public class ExportRecordResult {

  private Set<String> igsnInventoryLinkedItems;
}
