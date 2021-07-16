import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class UnUseStringFinder
{
	public static ArrayList<String> nameArrayList = new ArrayList<String>();
	public static ArrayList<String> resultArrayList = new ArrayList<String>();
	public static final String NAME_PRE_TAG = "name=";
	
	private static ActionListener mBtnListener = new BtnListener();
	
	private static JTextField folderTxt;
	private static JTextArea retLabel;
	private static ArrayList<String> printList = new ArrayList<String>();
	private static boolean mDoingWork = false;
	
	public static void main(String [] args) {
		JFrame window = new JFrame("Unuse string remove tool v0.1");
		window.setBounds(500, 100, 1000, 800);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		JPanel panel = new JPanel();
		JLabel guideLabel = new JLabel("Please input application folder (ex : \"D:/RightSideScreen\" : ");
		folderTxt = new JTextField(20);
		JButton button = new JButton("start");
		
		panel.add(guideLabel);
		panel.add(folderTxt);
		panel.add(button);
		button.addActionListener(mBtnListener);
		
		JPanel panel1 = new JPanel();
		JLabel loglabel = new JLabel("result log : ");
		panel1.add(loglabel);
		JPanel panel2 = new JPanel();
		retLabel = new JTextArea();
		//retLabel.setRows(25);
		JScrollPane scrollPane = new  JScrollPane(retLabel);
		scrollPane.setPreferredSize(new Dimension(800, 600));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel2.add(scrollPane);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(panel);
		mainPanel.add(panel1);
		mainPanel.add(panel2);
		window.add(mainPanel);
		window.setVisible(true);
	}
	
	static class BtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			String targetDir = String.valueOf(folderTxt.getText());
			if(targetDir == null ||  "".equals(targetDir)) {
				JOptionPane.showMessageDialog(null, "Please input application folder");
				return;
			}
			File file = new File(targetDir+"\\res\\values\\strings.xml");
			if(!file.exists()) {
				JOptionPane.showMessageDialog(null, "Please check application folder");
				return;
			}
			if(mDoingWork) {
				JOptionPane.showMessageDialog(null, " Please wait, in progress.");
				return;
			}
			nameArrayList.clear();
			resultArrayList.clear();
			new WorkThread(targetDir).start();
			
		}
		
	}
	
	private static class WorkThread extends Thread{
		private String mTargetDir;
		public WorkThread(String directory) {
			mTargetDir = directory;
			mDoingWork = true;
		}
		@Override
		public void run()
		{
			super.run();
			retLabel.setText("Please wait... doing");
			readStringNames(mTargetDir);
			checkingResourceName(mTargetDir);
			//System.out.println("=Unused string list =============");
			retLabel.setText("=Unused string list =============");
			for(String remainName : resultArrayList) {
				//printList.add("name = "+remainName);
				//System.out.println("name = "+remainName);
				String tempStr = retLabel.getText().toString()+"\n"+"name = "+remainName;
				retLabel.setText(tempStr);
			}
			retLabel.setText(retLabel.getText().toString());
			retLabel.setCaretPosition(retLabel.getDocument().getLength());
			//System.out.println("==========================");
			reWriteFile(mTargetDir+"\\res");
			mDoingWork = false;
			JOptionPane.showMessageDialog(null, " Done.");
		}
	}
	
	
	
	public static void readStringNames(String targetDir) {
		try {
			File file = new File(targetDir+"\\res\\values\\strings.xml");
	        FileReader filereader = new FileReader(file);
	        BufferedReader bufReader = new BufferedReader(filereader);

	        String line = "";
            while((line = bufReader.readLine()) != null){
	        	int aIdx = line.indexOf(NAME_PRE_TAG);
	        	int bIdx = -1;
	        	if(aIdx != -1) {
	        		aIdx = aIdx +NAME_PRE_TAG.length() + 1;
	        		bIdx = line.indexOf("\"", aIdx);
	        	}
	        	if(aIdx != -1 && bIdx != -1) {
	        		String nKey = line.substring(aIdx, bIdx);
	        		//System.out.println(nKey);
	        		nameArrayList.add(nKey);
	        	}
	        }
	        filereader.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void checkingResourceName(String dir) {
		resultArrayList.addAll(nameArrayList);
		for(String targetName : nameArrayList) {
			System.out.println("target = "+targetName);
			subDirList(dir, targetName);
		}
	}
	
	public static void subDirList(String filePath, String targetName){
        File file = new File(filePath); 
        File[] fileList = file.listFiles();
        try{
            for(File tmpFile : fileList){
                if(tmpFile.isFile() && !"strings.xml".equals(tmpFile.getName()) && !"Colors.xml".equalsIgnoreCase(tmpFile.getName())
                		&& !tmpFile.getName().contains(".jar") && !tmpFile.getName().contains(".png") && resultArrayList.contains(targetName)){
                   //System.out.println("\t 파일 = " + filePath+"/"+tmpFile.getName());
                    if(checkString(tmpFile, targetName)) {
                    	resultArrayList.remove(targetName);
    					System.out.println("find"+tmpFile.getName());
    					break;
    				}
                }else if(tmpFile.isDirectory() && !"gen".equals(tmpFile.getName()) && 
                		!filePath.contains("bin") && !filePath.contains(".idea") && !filePath.contains("libs")&&
                		!filePath.contains(".settings") && !filePath.contains(".git")){
                    //System.out.println("디렉토리 = " + filePath+"/"+tmpFile.getName());
                    subDirList(tmpFile.getCanonicalPath().toString(), targetName); //재귀 호출
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
	
	public static boolean checkString(File file, String name) {
		boolean hasName = false;
		FileReader filereader;
		try
		{
			String fileName = file.getName();
			if(fileName.contains(".java")) {
				name = "string."+name;
			}
			if(fileName.contains(".xml")) {
				name = "string/"+name;
			}
			filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			
            while((line = bufReader.readLine()) != null){
            	hasName = (line.indexOf(name) > -1);
            	if(hasName) {
            		break;
            	}
            }
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return hasName;
	}
	
	public static void reWriteFile(String filePath) {
		File file = new File(filePath); 
        File[] fileList = file.listFiles();
        try{
            for(File tmpFile : fileList){
                if(tmpFile.isFile() && "strings.xml".equals(tmpFile.getName())){
                   List<String> list = new ArrayList<String>();
                   List<String> newList = new ArrayList<String>();
                   Path path = Paths.get(tmpFile.getAbsolutePath());
                   list = Files.readAllLines(path, StandardCharsets.UTF_8);
                   for(String readLine : list) {
                	   boolean isContainsStr = false;
                	   for(String remainName : resultArrayList) {
               			String removeName = NAME_PRE_TAG+"\""+remainName+"\"";
	                	   if(readLine.contains(removeName)) {
	                		   isContainsStr = true;
	                		   break;
	                	   }
                	   }
                	   if(!isContainsStr) {
                		   newList.add(readLine);
                	   }
                   }
                   String newFileName = tmpFile.getParent().toString()+"\\"+tmpFile.getName();
                   File nFile = new File(newFileName); 
                   BufferedWriter bufferedWriter = //new BufferedWriter(new FileWriter(nFile));
                		   new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nFile.getPath()), "UTF-8"));
                   if(nFile.exists()) {
                	   nFile.delete();
                   }
                   if(nFile.isFile() && nFile.canWrite()) {
                	   for(String readLine : newList) {
                		   bufferedWriter.write(readLine);
                		   bufferedWriter.newLine();
                	   }
                	   bufferedWriter.close();
                   }
                }else if(tmpFile.isDirectory()) {
                	reWriteFile(tmpFile.getCanonicalPath().toString());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
	}
	
}
