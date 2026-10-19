import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;


public class Logger {
    
    
    //timestap for every log
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    //path of the all the file that are appended
    private final Path logFile;
    
    //if true enter to console
    private final boolean echoToConsole;
    
    public Logger(String fileName, boolean echoToConsole) {
        this.logFile = Paths.get(fileName);
        this.echoToConsole = echoToConsole;
    }
    
    //log a normal event
    public void log(String event) {
        write("INFO ", event);
    }
    
    
    //log an error
    public void logError(Exception ex) {
        write("ERROR", ex.getClass().getSimpleName() + ": " + ex.getMessage());
    }
    
    public List<String> readAll() {
        try {
            return Files.exists(logFile)
                    ? Files.readAllLines(logFile, StandardCharsets.UTF_8)
                    : Collections.<String>emptyList();
        } catch (IOException e) {
            return Collections.<String>emptyList();
        }
    }
    
    
    private synchronized void write(String level, String message) {
        String line = LocalDateTime.now().format(TIME_FORMAT) + " [" + level + "] " + message;
        try {
            Files.write(logFile, (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not write to log file: " + e.getMessage());
        }
        if (echoToConsole) {
            System.out.println("   [LOG] " + line);
        }
    }
    
    
}
