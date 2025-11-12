package com.researchspace.export.pdf;

import static com.researchspace.testutils.RSpaceTestUtils.setupVelocityWithTextFieldTemplates;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.researchspace.documentconversion.spi.ConversionResult;
import com.researchspace.documentconversion.spi.Convertible;
import com.researchspace.documentconversion.spi.DocumentConversionService;
import com.researchspace.linkedelements.RichTextUpdater;
import com.researchspace.model.EcatImage;
import com.researchspace.model.core.IRSpaceDoc;
import com.researchspace.model.record.TestFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class MSWordProcessorTest {
  MSWordProcessor mswordExporter;
  @Mock DocumentConversionService converter;
  @Mock ImageRetrieverHelper imageRetriever;
  @Mock IRSpaceDoc rspaceDoc;
  RichTextUpdater rtupdater;

  @BeforeEach
  public void setUp() throws Exception {
    mswordExporter = new MSWordProcessor();
    mswordExporter.setDocConverter(converter);
    mswordExporter.setImageHelper(imageRetriever);
    rtupdater = new RichTextUpdater();
    rtupdater.setVelocity(setupVelocityWithTextFieldTemplates());
  }

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testSupportsFormat() {
    assertTrue(mswordExporter.supportsFormat(ExportFormat.WORD));
    assertFalse(mswordExporter.supportsFormat(ExportFormat.PDF));
  }

  @Test
  public void testMakeExportThrowsIAEIfExportFormatisPdf() throws IOException {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          File outfile = File.createTempFile("any", ".doc");
          ExportToFileConfig cfg = getConfig();
          cfg.setExportFormat("PDF");
          ExportProcesserInput input = createAnyHTML();
          mswordExporter.makeExport(outfile, input, rspaceDoc, cfg);
          verify(imageRetriever, Mockito.never())
              .getImageBytesFromImgSrc(Mockito.anyString(), Mockito.any(ExportToFileConfig.class));
          verify(converter, never())
              .convert(Mockito.any(Convertible.class), Mockito.eq("doc"), Mockito.eq(outfile));
        });
  }

  @Test
  public void testMakeExportSimpleHappyCaseNoImages() throws IOException {
    File outfile = File.createTempFile("any", ".doc");
    ExportToFileConfig cfg = getConfig();
    ExportProcesserInput input = createAnyHTML();
    ConversionResult success = new ConversionResult(outfile, "ms/word");
    Mockito.when(
            converter.convert(
                Mockito.any(Convertible.class), Mockito.eq("doc"), Mockito.eq(outfile)))
        .thenReturn(success);
    mswordExporter.makeExport(outfile, input, rspaceDoc, cfg);
    Mockito.verify(imageRetriever, never())
        .getImageBytesFromImgSrc(Mockito.anyString(), Mockito.any(ExportToFileConfig.class));
    verify(converter, atMost(1))
        .convert(Mockito.any(Convertible.class), Mockito.eq("doc"), Mockito.eq(outfile));
  }

  @Test
  public void testMakeExportWithImages() throws IOException {
    File outfile = File.createTempFile("any", ".doc");
    ExportToFileConfig cfg = getConfig();
    ExportProcesserInput input = createAnyHTMLWithImage();
    ConversionResult success = new ConversionResult(outfile, "ms/word");
    when(converter.convert(Mockito.any(Convertible.class), Mockito.eq("doc"), Mockito.eq(outfile)))
        .thenReturn(success);
    when(imageRetriever.getImageBytesFromImgSrc(Mockito.anyString(), Mockito.eq(cfg)))
        .thenReturn(new byte[] {1, 2, 3, 4});
    mswordExporter.makeExport(outfile, input, rspaceDoc, cfg);
    verify(imageRetriever, atMost(1))
        .getImageBytesFromImgSrc(Mockito.anyString(), Mockito.any(ExportToFileConfig.class));
    verify(converter, atMost(1))
        .convert(Mockito.any(Convertible.class), Mockito.eq("doc"), Mockito.eq(outfile));
    System.err.println(outfile.length());
  }

  private ExportProcesserInput createAnyHTMLWithImage() {
    EcatImage image = TestFactory.createEcatImage(1L);
    String imgHtml = rtupdater.generateURLStringForEcatImageLink(image, 2 + "");
    return new ExportProcesserInput(imgHtml, Collections.emptyList(), null, null);
  }

  private ExportProcesserInput createAnyHTML() {
    return new ExportProcesserInput("<html/>", Collections.emptyList(), null, null);
  }

  private ExportToFileConfig getConfig() {
    ExportToFileConfig cfg = new ExportToFileConfig();
    cfg.setExportFormat("WORD");
    return cfg;
  }
}
