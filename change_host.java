import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;



public class change_host {
	public static void main(String[] args) {
		frame f = new frame();
		f.addComponent();
		f.addMenu();

	}

}

class frame extends JFrame {
	JFrame frame;
	JButton btn;
	JList list;
	JLabel lbplayer;
	File file;
	JLabel lbinfo;
	SV sv;

	frame() {
		frame = new JFrame("Stardew Valley Change Host Player");
		frame.setBounds(((Toolkit.getDefaultToolkit().getScreenSize().width)/2)-300, ((Toolkit.getDefaultToolkit().getScreenSize().height)/2)-300,600,600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(1, 3));
		frame.setSize(400, 200);
		frame.setLayout(new BorderLayout());
		frame.setVisible(true);
	}

	void addComponent() {
		JPanel jp1 = new JPanel();
		jp1.setLayout(new BorderLayout());
		JLabel label1 = new JLabel("当前农场主");
		lbplayer = new JLabel("none");
		jp1.add(label1, BorderLayout.NORTH);
		jp1.add(lbplayer, BorderLayout.CENTER);

		JPanel jp2 = new JPanel();
		jp2.setLayout(new BorderLayout());
		btn = new JButton("转换农场主");
		jp2.add(btn, BorderLayout.CENTER);

		JPanel jp3 = new JPanel();
		JLabel label2 = new JLabel("帮手：");
		jp3.add(label2);

		JPanel jpALL = new JPanel();
		jpALL.setLayout(new GridLayout(1, 3));
		jpALL.add(jp1);
		jpALL.add(jp2);
		jpALL.add(jp3);

		list = new JList();
		jp3.add(list);

		frame.add(jpALL, BorderLayout.CENTER);

		lbinfo = new JLabel("提示信息");
		frame.add(lbinfo, BorderLayout.SOUTH);

		
		frame.validate();
	}

	void addMenu() {
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("文件");
		JMenuItem item = new JMenuItem("导入");
		m.add(item);
		mb.add(m);
		frame.setJMenuBar(mb);
		frame.validate();
		JFileChooser fc = new JFileChooser(new File(System.getProperty("user.home")+"\\AppData\\Roaming\\StardewValley\\Saves"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//设置只打开文件夹
		item.addActionListener(e -> {
			int select = fc.showOpenDialog(this);// 显示打开文件对话框
			if (select == JFileChooser.APPROVE_OPTION)// 选择的是否为“确认”
			{
				file = fc.getSelectedFile();
				sv=new SV(file);
				reload(sv);
			} else {
				//打开操作被取消
			}
		});
		btn.addActionListener(e -> {
			if (!list.isSelectionEmpty()) {
				String username = (String) list.getSelectedValue();
				SV sv=new SV(file);
				sv.convey(username);
				lbinfo.setText("当前农场主是：" + username);
				this.reload(sv);
			} else {
				lbinfo.setText("noting selected");
			}
		});

	}
	
	void reload(SV sv) {
		sv.reLoadData();
		// TODO list增删
		DefaultListModel dlm = new DefaultListModel();
		for (int i = 0; i < sv.farmhand_name.length; i++) {
			dlm.addElement(sv.farmhand_name[i]);
		}
		list.setModel(dlm);
		;

		lbplayer.setText(sv.playername);
		validate();

	}
}

class SV {
	Document document = null;
	Node player = null;
	Node GameId=null;
	String playername = null;
	String[] farmhand_name = null;
	List<Node> farmhands = null;
	Node testfarmhand = null;
	File save_file;
	File GameInfoFile;
	SAXReader reader;
	
	SV(File file) {
		reader = new SAXReader();
		save_file=getSaveFile(file);
		GameInfoFile=getGameInfo(file);
		
		try {
			this.document = reader.read(save_file);
			this.player = document.selectSingleNode("/SaveGame/player");
			this.GameId=document.selectSingleNode("/SaveGame/uniqueIDForThisGame");
			this.playername = player.selectSingleNode("name").getText();
			this.farmhands = document.selectNodes(
					"/SaveGame/locations/GameLocation[@xsi:type='Farm']/buildings/Building/indoors[@xsi:type='Cabin']/farmhand");
			this.farmhand_name = get_farmhand_name();
			this.testfarmhand = farmhands.get(0);

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//将文件夹中的游戏进度文件返回
	File getSaveFile(File file) {
		String pattern = ".*_[0-9]{1,}";
		File[] tempList = file.listFiles();
		for(int i=0;i<tempList.length;i++) {
			String fileName=tempList[i].getName();
			if(Pattern.matches(pattern, fileName)) {
				return tempList[i];
			}
			
		}
		return new File("");
	}
	
	//将gameinfo文件返回
	File getGameInfo(File file) {
		File[] tempList = file.listFiles();
		for(int i=0;i<tempList.length;i++) {
			String fileName=tempList[i].getName();
			//这里注意String比较的方法，不能用==
			if(fileName.equals("SaveGameInfo")) {
				return tempList[i];
			}
			
		}
		return new File("");
	}

	// 获取帮手玩家名字
	String[] get_farmhand_name() {
		String[] names = new String[farmhands.size()];
		for (int i = 0; i < farmhands.size(); i++) {
			Node a = farmhands.get(i);
			names[i] = a.selectSingleNode("name").getText();
		}
		return names;
	}

	// 获取农场主数据,并将农场主数据清空
	ArrayList<Node> popPlayer() {
		ArrayList<Node> player_data = new ArrayList<>();
		List<Node> player_child = player.selectNodes("./*");
		for (int i = 0; i < player_child.size(); i++) {
			player_data.add(player_child.get(i));
			player_child.get(i).detach();
		}
		return player_data;
	}

	// 获取帮手数据，并将帮手数据清空
	ArrayList<Node> popFarmHand(String name) {
		ArrayList<Node> farmhand_data = new ArrayList<>();
		for (int i = 0; i < farmhand_name.length; i++) {
			if (name.equals(farmhand_name[i]) ) {
				testfarmhand = farmhands.get(i);
			}
		}
		List<Node> farmhand_child = testfarmhand.selectNodes("./*");
		for (int i = 0; i < farmhand_child.size(); i++) {
			farmhand_data.add(farmhand_child.get(i));
			farmhand_child.get(i).detach();
		}
		return farmhand_data;
	}

	// 交换数据并更改gameinfo文件中的name以及save中的gameid
	//此处的参数是帮手的name
	void convey(String name) {
		//更改uniqueIDForThisGame，因为官方文档中说要改，应该是不同存档要用不同的id
		this.GameId.setText("20000115");
		
		// 两个必须都先执行置空，再进行交换。如果置空一个就开始交换，那么第二个交换会将第一步的交换结果置空
		// 将两个节点内容取出并置空
		ArrayList<Node> farmhand_data = popFarmHand(name);
		ArrayList<Node> player_data = popPlayer();

		// 将农场主节点置入新的数据
		Element e_player = (Element) player;
		for (int i = 0; i < farmhand_data.size(); i++) {
			e_player.add(farmhand_data.get(i));
		}

		// 将帮手节点置入新的数据
		Element e_farmhand = (Element) testfarmhand;
		for (int i = 0; i < player_data.size(); i++) {
			e_farmhand.add(player_data.get(i));
		}
		
		//创建存档文件夹
		File dir=new File("./"+name+"_20000115");
		if(!dir.exists()) {
			dir.mkdir();
		}

		// 导出文件
		File file = new File("./"+name+"_20000115/"+name+"_20000115");
		XMLWriter writer;
		try {
			writer = new XMLWriter(new FileOutputStream(file));
			writer.write(document);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        //更改gameinfo里的name
		Document document2=null;
		try {
			//这里的document不能用之前定义那个，因为reload时需要原来的不变
			document2 = reader.read(GameInfoFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Node nameNode=document2.selectSingleNode("/Farmer/name");
		nameNode.setText(name);
		//写入文件
		file = new File("./"+name+"_20000115/SaveGameInfo");
        try {
            writer = new XMLWriter(new FileOutputStream(file));
            writer.write(document2);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }		
		
	}

	void reLoadData() {
		this.player = document.selectSingleNode("/SaveGame/player");
		this.playername = player.selectSingleNode("name").getText();
		this.farmhands = document.selectNodes(
				"/SaveGame/locations/GameLocation[@xsi:type='Farm']/buildings/Building/indoors[@xsi:type='Cabin']/farmhand");
		this.farmhand_name = get_farmhand_name();
	}
}
