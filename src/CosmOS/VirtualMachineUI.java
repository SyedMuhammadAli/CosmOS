package CosmOS;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.SpringLayout;
import javax.swing.JTextArea;
import java.awt.Component;
import javax.swing.Box;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import java.awt.GridLayout;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.JSplitPane;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.CardLayout;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JLabel;

public class VirtualMachineUI extends JFrame {
	//VM Objects
	private static Memory kingston;
	private static Processor intel;
	private static Scheduler processScheduler;
	private static LongTermScheduler programLoader;
	private static MemoryManagementUnit memManager;
	//VM Objects End ---
	
	private JPanel contentPane;
	
	//GUI Component Declaration
	JTextArea cosmosConsole = new JTextArea("Welcome to CosmOS Simulator.\n\n");; //text area for console
	JList list; //holds the list for process listing
	JRadioButton oneProcRadioBtn;
	JRadioButton allProcRadioBtn;
	ButtonGroup taskMgrRadioBtnGrp;
	JButton startBtn;
	JButton shutdownBtn;
	JPanel bootTab = new JPanel();
	JPanel taskMgrTab = new JPanel();
	JPanel advOptTab = new JPanel();
	JPanel displayTab = new JPanel();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VirtualMachineUI frame = new VirtualMachineUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public VirtualMachineUI() {
		setTitle("CosmOS Simulator v1.0 (alpha)");
		
		cosmosConsole.setVisible(false);
		cosmosConsole.setEnabled(false);
		
		//VM Constructor
		kingston			= new Memory();
		intel				= new Processor(kingston);
		memManager			= new MemoryManagementUnit(intel);
		kingston.setMemoryUnit(memManager);
		processScheduler	= new Scheduler(intel, kingston);
		programLoader		= new LongTermScheduler(kingston, processScheduler);
		//VM Constructor End ---
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 740, 480);
		setMaximizedBounds(getBounds());
		
		contentPane = new JPanel();
		setContentPane(contentPane);
		
		DefaultListModel procListModel = new DefaultListModel();
		procListModel.addElement("All Processes");
		procListModel.addElement("Blocked Processes");
		procListModel.addElement("Ready Processes");
		procListModel.addElement("Running Processes");
		
		cosmosConsole.setEditable(false);
		cosmosConsole.setLineWrap(true);
		cosmosConsole.setWrapStyleWord(true);
		
		JScrollPane scrollPane = new JScrollPane(cosmosConsole);
		scrollPane.setBounds(0, 0, 732, 291);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setVisible(true);
		contentPane.setLayout(null);
		contentPane.add(scrollPane);
		
		JTabbedPane buttonTabs = new JTabbedPane(JTabbedPane.LEFT);
		buttonTabs.setBounds(0, 296, 740, 148);
		buttonTabs.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(buttonTabs);
		buttonTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		//Boot Tab
		buttonTabs.addTab("Boot", null, bootTab, null);
		bootTab.setLayout(null);
		
		startBtn = new JButton("");
		startBtn.setBounds(10, 11, 64, 64);
		bootTab.add(startBtn);
		startBtn.setToolTipText("Startup VirtualMachine");
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cosmosConsole.setVisible(true);
				cosmosConsole.setEnabled(true);
				
				processScheduler.setDebugConsole(cosmosConsole);
			}
		});
		startBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/StartupVM.png")));
				
		shutdownBtn = new JButton("");
		shutdownBtn.setBounds(84, 11, 64, 64);
		bootTab.add(shutdownBtn);
		shutdownBtn.setToolTipText("Shutdown VirtualMachine");
		shutdownBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/ShutdownVM.png")));
		
		JButton hybernateBtn = new JButton("");
		hybernateBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/HibernateVM.png")));
		hybernateBtn.setToolTipText("Sends the VM into hybernate Mode");
		hybernateBtn.setBounds(158, 11, 64, 64);
		bootTab.add(hybernateBtn);
		shutdownBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cosmosConsole.setText(cosmosConsole.getText() + ">> Killing All Processes");
				cosmosConsole.setText(cosmosConsole.getText() + processScheduler.getProcessListStr("-a"));
				
				writeStringToFile(cosmosConsole.getText(), "ConsoleDump.doc");
				
				cosmosConsole.setVisible(false);
				cosmosConsole.setEnabled(false);
			}
		});
		//Task Manager Tab
		buttonTabs.addTab("Task Manager", null, taskMgrTab, null);
		taskMgrTab.setLayout(null);
		
		JButton loadProcBtn = new JButton("");
		loadProcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				int r_value = fileChooser.showOpenDialog(getParent());
				
				if(r_value == JFileChooser.APPROVE_OPTION){
					programLoader.load(fileChooser.getSelectedFile().getName());
				}
			}
		});
		loadProcBtn.setToolTipText("Load Process Into Ready Queue");
		loadProcBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/LoadProcess.png")));
		loadProcBtn.setBounds(10, 11, 64, 64);
		taskMgrTab.add(loadProcBtn);
		
		JButton killProcBtn = new JButton("");
		killProcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int tmpProcId = readProcIdFromUser();
				if(tmpProcId == -1)
					return; //If invalid proc Id was encountered.

				appendTextToConsole(">> Killing Process with PID " + tmpProcId);
				processScheduler.killProcess(tmpProcId);
			}
		});
		killProcBtn.setToolTipText("Remove Process from all Queues");
		killProcBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/KillProcess.png")));
		killProcBtn.setBounds(84, 11, 64, 64);
		taskMgrTab.add(killProcBtn);
		
		//Radio Buttons
		oneProcRadioBtn = new JRadioButton("One Process");
		oneProcRadioBtn.setToolTipText("Perform the following actions on one process only.");
		oneProcRadioBtn.setBounds(166, 15, 109, 23);
		taskMgrTab.add(oneProcRadioBtn);
		
		allProcRadioBtn = new JRadioButton("All Processes");
		allProcRadioBtn.setToolTipText("Perform the following options on all processes in the ready queue.");
		allProcRadioBtn.setBounds(166, 48, 109, 23);
		taskMgrTab.add(allProcRadioBtn);
		
		//Radio Buttons and Btn Group
		taskMgrRadioBtnGrp = new ButtonGroup();
		taskMgrRadioBtnGrp.add(oneProcRadioBtn);
		taskMgrRadioBtnGrp.add(allProcRadioBtn);
		
		//Execution Buttons
		JButton execProcBtn = new JButton("");
		execProcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(allProcRadioBtn.isSelected()){ //do for all processes
					cosmosConsole.append(">> Execute All Processes\n");
					processScheduler.run();
					cosmosConsole.append("Done; Terminating Process(es)...\n");
				} else { //do for one process
					int tmpProcId = readProcIdFromUser();
					if(tmpProcId == -1) return; //If invalid proc Id was encountered.

					PCB tmpPcbPtr = processScheduler.getPCB(tmpProcId);

					cosmosConsole.append(">> Executed Processes with ID " + tmpProcId + "\n");

					//Switch Context only if the given process is not already running
					if( intel.currentProcess != null &&
							intel.currentProcess.getPID() != tmpPcbPtr.getPID() ){

						intel.switchProcessTo(tmpPcbPtr);
					} else if( intel.currentProcess == null ) {
						intel.switchProcessTo(tmpPcbPtr);
					}

					if(intel.currentProcess != null){
						while(intel.execNextIntruction()); //completely execute the process
						
						cosmosConsole.append("Done; Terminating Process(es)...\n");
						processScheduler.killProcess(tmpProcId); //remove process from all queues
					} else {
						cosmosConsole.append("Error: Something really bad happened @ debugProcBtn Event Handler.\n");
					}
				}
			}
		});
		execProcBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/ExecuteProcess.png")));
		execProcBtn.setToolTipText("Execute Process(es)");
		execProcBtn.setBounds(281, 11, 64, 64);
		taskMgrTab.add(execProcBtn);
		
		JButton debugProcBtn = new JButton("");
		debugProcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(allProcRadioBtn.isSelected()){ //do for all processes
					cosmosConsole.append(">> Debug All Processes\n");
					processScheduler.enableDebugging(true);
					processScheduler.run();
					processScheduler.enableDebugging(false);
					cosmosConsole.append("Done; Terminating Process(es)...\n");
				} else { //do for one process
					int tmpProcId = readProcIdFromUser();
					if(tmpProcId == -1) return; //If invalid proc Id was encountered.
					
					PCB tmpPcbPtr = processScheduler.getPCB(tmpProcId);
					
					cosmosConsole.append(">> Debug Processes with ID " + tmpProcId + "\n");
					
					//Switch Context only if the given process is not already running
					if( intel.currentProcess != null &&
						intel.currentProcess.getPID() != tmpPcbPtr.getPID() ){
						
						intel.switchProcessTo(tmpPcbPtr);
					} else if( intel.currentProcess == null ) {
						intel.switchProcessTo(tmpPcbPtr);
					}
					
					if(intel.currentProcess != null){
						intel.execNextIntruction();
						cosmosConsole.append(intel.getDebugInfoString());
					} else {
						cosmosConsole.append("Error: Something really bad happened @ debugProcBtn Event Handler.\n");
					}
				}
			}
		});
		debugProcBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/DebugProcess.png")));
		debugProcBtn.setToolTipText("Debug a process");
		debugProcBtn.setBounds(355, 11, 64, 64);
		taskMgrTab.add(debugProcBtn);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(158, 11, 2, 68);
		taskMgrTab.add(separator);
		
		//Advance Options Tab
		buttonTabs.addTab("Advanced Options", null, advOptTab, null);
		advOptTab.setLayout(null);
		
		JButton blockProcBtn = new JButton("");
		blockProcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int tmpProcId = readProcIdFromUser();
				if(tmpProcId == -1) return; //If invalid proc Id was encountered.
				
				cosmosConsole.append(">> Blocking Process with ID " + tmpProcId + "\n");
				processScheduler.blockProcess(tmpProcId);
				cosmosConsole.append("Done.\n");
			}
		});
		blockProcBtn.setToolTipText("Blocks the process with the given PID");
		blockProcBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/BlockProcess.png")));
		blockProcBtn.setBounds(10, 11, 64, 64);
		advOptTab.add(blockProcBtn);
		
		JButton unblockProcBtn = new JButton("");
		unblockProcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int tmpProcId = readProcIdFromUser();
				if(tmpProcId == -1) return; //If invalid proc Id was encountered.
				
				cosmosConsole.append(">> Unblocking Process with ID " + tmpProcId + "\n");
				processScheduler.unblockProcess(tmpProcId);
				cosmosConsole.append("Done.\n");
			}
		});
		unblockProcBtn.setToolTipText("Unblocks the proces with the given PID");
		unblockProcBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/UnblockProcess.png")));
		unblockProcBtn.setBounds(84, 11, 64, 64);
		advOptTab.add(unblockProcBtn);
		
		JButton cloneProcBtn = new JButton("");
		cloneProcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int tmpProcId = readProcIdFromUser();
				if(tmpProcId == -1) return; //If invalid proc Id was encountered.
				
				PCB tmpPcbPtr = processScheduler.getPCB(tmpProcId);
				
				cosmosConsole.append(">> Cloning process with id " + tmpProcId + "\n");
				programLoader.cloneProcess(tmpPcbPtr);
				cosmosConsole.append("Done.\n");
			}
		});
		cloneProcBtn.setToolTipText("Clone the process with the given PID");
		cloneProcBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/CreateClone.png")));
		cloneProcBtn.setBounds(158, 11, 64, 64);
		advOptTab.add(cloneProcBtn);
		
		JSeparator advTabSeperator = new JSeparator();
		advTabSeperator.setOrientation(SwingConstants.VERTICAL);
		advTabSeperator.setBounds(232, 11, 2, 68);
		advOptTab.add(advTabSeperator);
		
		JButton memoryDumpBtn = new JButton("");
		memoryDumpBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/MemoryDump.png")));
		memoryDumpBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int tmpProcId = readProcIdFromUser();
				String dumpStr = new String();
				if(tmpProcId == -1)
					return; //If invalid proc Id was encountered.
				
				//All Should be well beyond this point
				PCB tmpPcbPtr = processScheduler.getPCB(tmpProcId);

				cosmosConsole.append(">> Memory Dump for Proceess " + tmpProcId + "\n");
				
				for(int i=0; i<tmpPcbPtr.getPageTableSize(); i++){ //iterate over frames
					int base = tmpPcbPtr.getPageTableEntry(i) * kingston.FRAME_SIZE;
					
					for(int addr=base; addr<(base+kingston.FRAME_SIZE); addr++){//iterate over offset
						cosmosConsole.append(String.format("%h\n", kingston.readByteFromPhysical(addr)));
						
						//Make Dump String
						dumpStr += String.format("%h\n", kingston.readByteFromPhysical(addr));
					}
				}
				
				writeStringToFile(dumpStr, tmpProcId + "_" + tmpPcbPtr.getBaseFrame() + " Memory Dump.doc");
				cosmosConsole.append("Done.\n");
			}
		});
		memoryDumpBtn.setToolTipText("Dump memory for the given prcoess.");
		memoryDumpBtn.setBounds(244, 11, 64, 64);
		advOptTab.add(memoryDumpBtn);
		
		JButton memDetailsBtn = new JButton("");
		memDetailsBtn.setToolTipText("Display memory details for the given process.");
		memDetailsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int tmpProcId = readProcIdFromUser();
				if(tmpProcId == -1)
					return; //If invalid proc Id was encountered.
				
				//All Should be well beyond this point
				PCB tmpPcbPtr = processScheduler.getPCB(tmpProcId);
				
				appendTextToConsole(">> Display Memorty Details for PID " + tmpProcId);
				appendTextToConsole(String.format("PCB Location\t%h", tmpPcbPtr.getBaseFrame()));
				appendTextToConsole("Page #\tFrame #\tPhysical Address");
				
				for(int i=0; i<tmpPcbPtr.getPageTableSize(); i++){
					appendTextToConsole(i + "\t" + tmpPcbPtr.getPageTableEntry(i).toString() + "\t" + (tmpPcbPtr.getPageTableEntry(i)*kingston.FRAME_SIZE) );
				}
			}
		});
		memDetailsBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/MemoryDetails.png")));
		memDetailsBtn.setBounds(318, 11, 64, 64);
		advOptTab.add(memDetailsBtn);
		
		//Display Tab
		buttonTabs.addTab("Display Commands", null, displayTab, null);
		displayTab.setLayout(null);
		
		list = new JList();
		list.setModel(procListModel);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setBounds(10, 11, 111, 72);
		displayTab.add(list);
		
		JButton listProcBtn = new JButton("");
		listProcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(list.getSelectedValue() == "All Processes")
					cosmosConsole.setText(cosmosConsole.getText() + ">> All Processes\n" + processScheduler.getProcessListStr("-a"));
				else if(list.getSelectedValue() == "Blocked Processes")
					cosmosConsole.setText(cosmosConsole.getText() + ">> Blocked Processes\n" + processScheduler.getProcessListStr("-b"));
				else if(list.getSelectedValue() == "Ready Processes")
					cosmosConsole.setText(cosmosConsole.getText() + ">> Ready Processes\n" + processScheduler.getProcessListStr("-r"));
				else if(list.getSelectedValue() == "Running Processes")
					cosmosConsole.setText(cosmosConsole.getText() + ">> Running Processes\n" + processScheduler.getProcessListStr("-es"));
			}
		});
		
		listProcBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/ListProcessQueue.png")));
		listProcBtn.setToolTipText("List process queues");
		listProcBtn.setBounds(131, 11, 64, 64);
		displayTab.add(listProcBtn);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(205, 11, 2, 68);
		displayTab.add(separator_1);
		
		JButton displayPcbBtn = new JButton("");
		displayPcbBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int tmpProcId = readProcIdFromUser();
				if(tmpProcId == -1)
					return; //If invalid proc Id was encountered.
				
				//All Should be well beyond this point
				PCB tmpPcbPtr = processScheduler.getPCB(tmpProcId);
				
				cosmosConsole.setText(cosmosConsole.getText() + ">> Display PCB [id: " + tmpProcId + "]\n");
				cosmosConsole.setText(cosmosConsole.getText() + tmpPcbPtr.getPcbAsString());
			}
		});
		displayPcbBtn.setToolTipText("Display the PCB of the given process.");
		displayPcbBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/DisplayPCB.png")));
		displayPcbBtn.setBounds(217, 11, 64, 64);
		displayTab.add(displayPcbBtn);
		
		JButton displayPageTableBtn = new JButton("");
		displayPageTableBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int tmpProcId = readProcIdFromUser();
				if(tmpProcId == -1)
					return; //If invalid proc Id was encountered.
				
				//All Should be well beyond this point
				PCB tmpPcbPtr = processScheduler.getPCB(tmpProcId);
				
				appendTextToConsole(">> Display PageTable for PID " + tmpProcId);
				appendTextToConsole("Page #\tFrame #");
				for(int i=0; i<tmpPcbPtr.getPageTableSize(); i++){
					appendTextToConsole(i + "\t" + tmpPcbPtr.getPageTableEntry(i).toString());
				}
			}
		});
		displayPageTableBtn.setToolTipText("Display the Page Table of the given process.");
		displayPageTableBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/DisplayPageTable.png")));
		displayPageTableBtn.setBounds(291, 11, 64, 64);
		displayTab.add(displayPageTableBtn);
		
		JButton displayRegistersBtn = new JButton("");
		displayRegistersBtn.setToolTipText("Print the states of the CPU Registers.");
		displayRegistersBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cosmosConsole.setText(cosmosConsole.getText() + intel.getRegisterInfoStr());
			}
		});
		displayRegistersBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/DisplayRegisters.png")));
		displayRegistersBtn.setBounds(365, 11, 64, 64);
		displayTab.add(displayRegistersBtn);
		
		JButton displayFreeFramesBtn = new JButton("");
		displayFreeFramesBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cosmosConsole.setText(cosmosConsole.getText() + kingston.getAvailableFrameInfoStr());
			}
		});
		displayFreeFramesBtn.setToolTipText("Display the free frame list.");
		displayFreeFramesBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/FreeFrames.png")));
		displayFreeFramesBtn.setBounds(439, 11, 64, 64);
		displayTab.add(displayFreeFramesBtn);
		
		JButton displayAllocatedFramesBtn = new JButton("");
		displayAllocatedFramesBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinkedList<PCB> tmpProcList = processScheduler.getProcessList();
				
				appendTextToConsole(">> Allocated Frames Information");
				appendTextToConsole("PID\tFrame List");
				for(int i=0; i<tmpProcList.size(); i++){
					PCB tmpPcbPtr = tmpProcList.get(i);
					cosmosConsole.append(tmpPcbPtr.getPID() + "\t[ "); //print pid
					
					for(int j=0; j<tmpPcbPtr.getPageTableSize(); j++){ //print list
						cosmosConsole.append(tmpPcbPtr.getPageTableEntry(j) + " ");
					}
					
					cosmosConsole.append("]\n"); //print closing bracket
				}
			}
		});
		displayAllocatedFramesBtn.setToolTipText("Display the list of frames allocated to processes.");
		displayAllocatedFramesBtn.setIcon(new ImageIcon(VirtualMachineUI.class.getResource("/icons/AllocatedFrames.png")));
		displayAllocatedFramesBtn.setBounds(513, 11, 64, 64);
		displayTab.add(displayAllocatedFramesBtn);
	}
	
	//Class Utility Funtions
	
	/* readProcIdFromUser
	 * Displays a Input dialog box and returns a valid process Id
	 * read from the user. Sends error msg to the console otherwise. Also,
	 * return -1 on failure.
	 */
	private int readProcIdFromUser(){
		String tmpStr = JOptionPane.showInputDialog("Enter the process ID");
		int pid = -1;
		
		try{
			pid = Integer.parseInt(tmpStr);
		} catch(NumberFormatException nfe) {
			cosmosConsole.append("Error: Process ID must be an integer!\n");
			return -1;
		}
		
		if(processScheduler.getPCB(pid) == null){
			cosmosConsole.append("Error: No process exists with the given process id!\n");
			
			return -1;
		} else {
			return pid;
		}
	}
	
	/* appendTextToConsole - Appends text to console. Attaches a newline in the end. */
	private void appendTextToConsole(String msg){
		cosmosConsole.append(msg + "\n");
	}
	
	public void writeStringToFile(String text, String fileName){
		try{
			// Create file 
			FileWriter fstream = new FileWriter(fileName);
	        BufferedWriter out = new BufferedWriter(fstream);	//object to handle file
			out.write(text);	//writting the input text
			//Close the output stream
			out.close();
			}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
	}
}
