import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
/**
 *
 * @author ngculz20
 */
public class FileChooser extends JFrame implements ActionListener{
    
    FileChooser(){
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLayout(new FlowLayout());
    this.pack();
    this.setVisible(true);
    }
    
    JButton button = new JButton("Select File");

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if(e.getSource()==button){
          JFileChooser fileChooser = new JFileChooser();
          fileChooser.setCurrentDirectory(new File(".")); //sets current directory
			
			int response = fileChooser.showOpenDialog(null); //select file to open
			//int response = fileChooser.showSaveDialog(null); //select file to save
			
			if(response == JFileChooser.APPROVE_OPTION) {
				File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
				System.out.println(file);
			}
        }
        
    }
    
    
}
