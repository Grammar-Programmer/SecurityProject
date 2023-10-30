import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class OpenRecentFiles {
   private Desktop desktop;
   private File[] recentFiles;
   private ArrayList<Process> processesOpened = new ArrayList<>();
   private String site;

   // Constructors
   public OpenRecentFiles(Desktop desktop, String site) {
      this.desktop = desktop;
      this.site = site;

      String path = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Recent";
      File file = new File(path);

      this.recentFiles = file.listFiles();

      if (this.recentFiles != null) {
         Arrays.sort(this.recentFiles, Comparator.comparing(File::lastModified).reversed());
      }

   }

   // Methods
   void openURI() {
      if (this.desktop.isSupported(Action.BROWSE)) {
         try {
            URI uri = new URI(site);
            this.desktop.browse(uri);
         } catch (URISyntaxException | IOException e) {
         }
      }
   }

   void openFile(String path) {
      try {
         String os = System.getProperty("os.name").toLowerCase();
         ProcessBuilder pb;
         if (os.contains("win"))
            pb = new ProcessBuilder(new String[] { "cmd", "/c", "start", "\"\"", path });
         else
            pb = new ProcessBuilder(new String[] { "xdg-open", path });

         this.processesOpened.add(pb.start());
      } catch (IOException e) {
      }

   }

   // Statics
   public static boolean isValidURI(String uri) {
      final URL url;
      try {
         url = new URL(uri);
      } catch (Exception e1) {
         return false;
      }
      return "http".equals(url.getProtocol());
   }

   public static boolean isValidOS() {
      // Only works on Windows
      String os = System.getProperty("os.name").toLowerCase();
      if (!os.contains("win")) {
         System.out.println("Unsupported operating system.");
         return false;
      }

      return true;
   }

   public static String formatSite(String[] args) {
      if (args.length < 1 || !isValidURI(args[0])) {
         System.err.println("Insert a valid URI in the first argument next time ;)");
         return "https://github.com/Grammar-Programmer";
      }
      return args[0];
   }

   public static void main(String[] args) throws InterruptedException, IOException {
      if (!isValidOS())
         return;

      if (Desktop.isDesktopSupported()) {
         OpenRecentFiles openRecentFiles = new OpenRecentFiles(Desktop.getDesktop(), formatSite(args));

         // Perform action before it closes
         Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            openRecentFiles.openURI();
         }));

         // Open recent File every 2 seconds
         for (File f : openRecentFiles.recentFiles) {
            openRecentFiles.openFile(f.getAbsolutePath());
            Thread.sleep(2000L);
         }

      }
   }
}
