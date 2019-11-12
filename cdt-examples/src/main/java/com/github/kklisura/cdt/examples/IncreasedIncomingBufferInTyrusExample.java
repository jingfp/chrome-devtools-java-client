package com.github.kklisura.cdt.examples;

import com.github.kklisura.cdt.launch.ChromeLauncher;
import com.github.kklisura.cdt.protocol.commands.Page;
import com.github.kklisura.cdt.protocol.types.page.PrintToPDF;
import com.github.kklisura.cdt.protocol.types.page.PrintToPDFTransferMode;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.ChromeService;
import com.github.kklisura.cdt.services.factory.impl.ConfigurableTyrusClientFactory;
import com.github.kklisura.cdt.services.types.ChromeTab;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * This example demonstrates how to increase incoming buffer size in Tyrus client. Use the following
 * example to fix the issues with Tyrus Buffer overflow. This issue occurs when the incoming message
 * size is larger than the incoming buffer in Tyrus client at which the client disconnects.
 *
 * @author Kenan Klisura
 */
public class IncreasedIncomingBufferInTyrusExample {
  static {
    System.setProperty(
        "com.github.kklisura.cdt.services.config.webSocketContainerFactory",
        ConfigurableTyrusClientFactory.class.getName());
  }

  public static void main(String[] args) {
    // Create chrome launcher.
    final ChromeLauncher launcher = new ChromeLauncher();

    // Launch chrome either as headless (true) - PDF printing is only supported on Chrome headless
    // at the moment
    final ChromeService chromeService = launcher.launch(true);

    // Create empty tab ie about:blank.
    final ChromeTab tab = chromeService.createTab();

    // Get DevTools service to this tab
    final ChromeDevToolsService devToolsService = chromeService.createDevToolsService(tab);

    // Get individual commands
    final Page page = devToolsService.getPage();
    page.enable();

    // Navigate to github.com.
    page.navigate("https://github.com");

    page.onLoadEventFired(
        loadEventFired -> {
          System.out.println("Printing to PDF...");

          final String outputFilename = "test.pdf";

          Boolean landscape = false;
          Boolean displayHeaderFooter = false;
          Boolean printBackground = false;
          Double scale = 1d;
          Double paperWidth = 8.27d; // A4 paper format
          Double paperHeight = 11.7d; // A4 paper format
          Double marginTop = 0d;
          Double marginBottom = 0d;
          Double marginLeft = 0d;
          Double marginRight = 0d;
          String pageRanges = "";
          Boolean ignoreInvalidPageRanges = false;
          String headerTemplate = "";
          String footerTemplate = "";
          Boolean preferCSSPageSize = false;
          PrintToPDFTransferMode mode = PrintToPDFTransferMode.RETURN_AS_BASE_64;

          dump(
              outputFilename,
              devToolsService
                  .getPage()
                  .printToPDF(
                      landscape,
                      displayHeaderFooter,
                      printBackground,
                      scale,
                      paperWidth,
                      paperHeight,
                      marginTop,
                      marginBottom,
                      marginLeft,
                      marginRight,
                      pageRanges,
                      ignoreInvalidPageRanges,
                      headerTemplate,
                      footerTemplate,
                      preferCSSPageSize,
                      mode));

          System.out.println("Done!");
          devToolsService.close();
        });

    devToolsService.waitUntilClosed();
  }

  private static void dump(String fileName, PrintToPDF data) {
    FileOutputStream fileOutputStream = null;
    try {
      File file = new File(fileName);
      fileOutputStream = new FileOutputStream(file);
      fileOutputStream.write(Base64.getDecoder().decode(data.getData()));
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (fileOutputStream != null) {
        try {
          fileOutputStream.flush();
          fileOutputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}