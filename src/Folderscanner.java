import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


public class Folderscanner {
    
    
    private final List<Path> fileList = new ArrayList<>();
    
    
    //goes through the folder tree
    public void scanRecursive(Path dir) throws IOException {
        fileList.clear();
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile).forEach(fileList::add);
        }
        Collections.sort(fileList);                 // stable, readable order
    }
    
    
    public List<Path> getFileList(){
        return Collections.unmodifiableList(new ArrayList<>(fileList));
    }
}
