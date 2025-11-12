package com.researchspace.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.researchspace.client.BioPortalOntologiesClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.stereotype.Service;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@Slf4j
@Service
public class BioPortalOntologiesServiceTest {
  public static final String realBioData =
      "Ctenotus"
          + " hanloni|http://purl.bioontology.org/ontology/NCBITAXON/480744||NCBITAXON|http://purl.bioontology.org/ontology/NCBITAXON/480744|Ctenotus"
          + " hanloni||National Center for Biotechnology Information (NCBI) Organismal"
          + " Classification|NCBITAXON|~!~Ctenotus"
          + " hanloni|http://purl.obolibrary.org/obo/VTO_0018361||VTO|http://purl.obolibrary.org/obo/VTO_0018361|Ctenotus"
          + " hanloni||Vertebrate Taxonomy Ontology|VTO|";
  @Mock private BioPortalOntologiesClient bioOntologiesClientMock;
  @InjectMocks private BioPortalOntologiesService testee;

  @BeforeEach
  public void setUp() {
    when(bioOntologiesClientMock.getBioOntologyData(any(String.class))).thenReturn("");
  }

  @Test
  public void shouldSwallowExceptions() {
    doThrow(RuntimeException.class)
        .when(bioOntologiesClientMock)
        .getBioOntologyData(any(String.class));
    assertEquals(0, testee.getBioOntologyDataForQuery("abc").size());
  }

  @Test
  public void shouldReturnEmptyListWhenNoBioData() {
    assertEquals(0, testee.getBioOntologyDataForQuery("").size());
  }

  @Test
  public void shouldReturnEmptyListWhenFilterTermLessThanTwoChars() {
    when(bioOntologiesClientMock.getBioOntologyData(any(String.class))).thenReturn(realBioData);
    assertEquals(0, testee.getBioOntologyDataForQuery("aa").size());
  }

  @Test
  public void shouldReturnFormattedValuesWhenFilterMatchesDataReturnedFromBioPortal() {
    when(bioOntologiesClientMock.getBioOntologyData(any(String.class))).thenReturn(realBioData);
    List<String> data = testee.getBioOntologyDataForQuery("cen");
    assertEquals(2, data.size());
    assertTrue(
        data.contains(
            "Ctenotus"
                + " hanloni__RSP_EXTONT_URL_DELIM__http://purl.bioontology.org/ontology/NCBITAXON/480744__RSP_EXTONT_NAME_DELIM__NCBITAXON__RSP_EXTONT_VERSION_DELIM__https://bioportal.bioontology.org/ontologies/NCBITAXON"
                + "  on: "
                + getTodayDateFormatted()));
    assertTrue(
        data.contains(
            "Ctenotus"
                + " hanloni__RSP_EXTONT_URL_DELIM__http://purl.obolibrary.org/obo/VTO_0018361__RSP_EXTONT_NAME_DELIM__VTO__RSP_EXTONT_VERSION_DELIM__https://bioportal.bioontology.org/ontologies/VTO"
                + "  on: "
                + getTodayDateFormatted()));
  }

  private String getTodayDateFormatted() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDateTime now = LocalDateTime.now();
    return dtf.format(now);
  }
}
