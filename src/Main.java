import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;




public class Main {
    
    
    private static final String DEMO_DIR = "demo_data";
    
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    
    private static final Scanner INPUT = new Scanner(System.in);
    
    public static void main(String[] args) {
        
        Logger logger = new Logger("app.log", true);
        Keymanager keyManager = new Keymanager("auth.dat");
        Encryptionalgorithm algorithm = new Customcipher();
        Folderscanner scanner = new Folderscanner();
        Fileprocessor processor = new Fileprocessor(algorithm, scanner, keyManager, logger);
        
        if (args.length > 0 && args[0].equalsIgnoreCase("--gui")){
            new GUI(processor).buildWindow();
            return;
        }
        
        
        System.out.println("=================================");
        System.out.println("   CUSTOM FILE ENCRYPTION TOOL   ");
        System.out.println("=================================");
        
        if (!login(keyManager, logger)) {
            System.out.println("Too many failed attempts. Exiting.");
        }
        
        runMenu(processor, scanner, logger);
    }
    
    
    /*Login system*/
    
    private static boolean login(Keymanager keyManager, Logger logger){
        try{
            if(!keyManager.hasCredentials()) {
                System.out.println("First run: create a master password (min. 6 characters)");
                
                while(true) {
                    
                    char[] first = readPassword("Create a master password: ");
                    char[] second = readPassword("Confirm master password: ");
                    
                    if(first.length < 6){
                        System.out.println("Too short, try again.");
                    } else if (!Arrays.equals(first, second)){
                        System.out.println("Passwords dont match.");
                    } else {
                        keyManager.registerMasterPassword(first);
                        logger.log("Master password created");
                        return true;
                    }
                }
            }
            for(int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++){
                char[] pwd = readPassword("Master password: ");
                if(keyManager.authenticate(pwd)){
                    logger.log("Login succesful");
                    return true;
                }
                logger.log("Failed login attempt " + attempt + "/" + MAX_LOGIN_ATTEMPTS);
            }
        } catch (IOException e){
            logger.logError(e);
        }
        
        return false;
        
    }
    
    
    
    //MENU
    
    private static void runMenu(Fileprocessor processor, Folderscanner scanner, Logger logger){
        while (true){
            System.out.println("\n-------MENU-------\n");
            System.out.println("1) Create sample files (" + DEMO_DIR + ")");
            System.out.println("2) List files in a folder");
            System.out.println("3) Encrypt a folder");
            System.out.println("4) Decrypt a folder");
            System.out.println("5) View activity log");
            System.out.println("6) Exit");
            System.out.println("Choice: ");
            
            switch(INPUT.nextLine().trim()){
                case "1":
                    createSampleFiles(logger);
                    break;
                case "2":
                    listFiles(scanner, logger);
                    break;
                case "3":
                    processFolder(processor, true, logger);
                    break;
                case "4":
                    processFolder(processor, false, logger);
                    break;
                case "5":
                    showLog(logger);
                    break;
                case "6":
                    logger.log("Application closed");
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }
    }
    
    private static void createSampleFiles(Logger logger){
        try{
            Path root = Paths.get(DEMO_DIR);
            Files.createDirectories(root.resolve("reports/2026"));
            write(root.resolve("notes.txt"), "Meeting notes: project demo on Friday.");
            write(root.resolve("passwords.txt"), "wifi=SuperSecret123\nbank=do-not-share");
            write(root.resolve("reports/summary.csv"), "name,score\nAlice,90\nBob,85");
            write(root.resolve("reports/2026/q1.txt"), "Q1 revenue: 12,500");
            logger.log("Sample files created in '" + DEMO_DIR + "'");
        } catch (IOException e){
            logger.logError(e);
        }
    }
    
    
    private static void listFiles(Folderscanner scanner, Logger logger){
        
        
        Path dir = Paths.get(ask("Folder to list [" + DEMO_DIR + "]: ", DEMO_DIR));
        try {
            scanner.scanRecursive(dir);
            List<Path> files = scanner.getFileList();
            System.out.println("Found " + files.size() + " file(s):");
            for (Path f : files) {
                System.out.println("  " + f + "  (" + Files.size(f) + " bytes)");
            }
        } catch (IOException e) {
            logger.logError(e);
        }
    }
    
    
    private static void processFolder(Fileprocessor processor, boolean encrypt, Logger logger){
        
        Path dir = Paths.get(ask("Folder [" + DEMO_DIR + "}:", DEMO_DIR));
        char[] pwd = readPassword("Folder password: ");
        
        try {
            if (encrypt) {
                if (!ask("Originals are replaced by .enc files. Continue? (y/n) [y]: ", "y")
                        .equalsIgnoreCase("y")) {
                    return;
                }
                int n = processor.encryptFolder(dir, pwd);
                System.out.println("Result: " + n + " file(s) encrypted.");
            } else {
                int n = processor.decryptFolder(dir, pwd);
                System.out.println("Result: " + n + " file(s) decrypted.");
            }
        } catch (Exception e){
            logger.logError(e);
        }
    }
    
    
    private static void showLog(Logger logger){
        System.out.println("\n----- app.log -----\n");
        for (String line : logger.readAll()){
            System.out.println(line);
        }
    }
    
    
    
    
    //--------HELPER METHODS--------//
    
    //this function prints the whole log file
    private static void write(Path path, String text) throws IOException{
        
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
    }
    
    //this funtion prompts for a line of text and returns when the user presses Enter
    private static String ask(String prompt, String defaultValue) {
        System.out.print(prompt);
        String line = INPUT.nextLine().trim();
        return line.isEmpty() ? defaultValue : line;
    }
    
    
    private static char[] readPassword(String prompt){
        
        Console console = System.console();
        if(console != null) {
            return console.readPassword(prompt);
        }
        
        System.out.print(prompt);
        return INPUT.nextLine().toCharArray();
    }
}
