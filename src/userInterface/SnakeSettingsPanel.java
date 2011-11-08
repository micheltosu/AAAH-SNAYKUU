package userInterface;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import gameLogic.*;
import javax.swing.filechooser.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import static java.awt.GridBagConstraints.*;

class SnakeSettingsPanel extends JPanel
{
	private JList snakeJList;
	private JList brainJList;
	private JButton addSnakeButton;
	private JButton removeSnakeButton;
	private JButton reloadAllBrainsButton;
	private Map<String, String> snakes = new TreeMap<String, String>();
	private Map<String, Class<? extends Brain>> brains = new TreeMap<String, Class<? extends Brain>>();
	
	public SnakeSettingsPanel()
	{
		 //~Cool version - looks better but is buggy atm
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		
		GridBagConstraints constraint = new GridBagConstraints();
		
		
		constraint.fill = BOTH;
		constraint.gridwidth = 2;
		constraint.gridheight = 8;
		constraint.weightx = 1.0;
		
		constraint.gridy = 0;
		
			constraint.gridx = 0;
			constraint.anchor = WEST;
			
				snakeJList = new JList();
				snakeJList.addMouseListener(new SnakeMouseListener());
				
				JScrollPane jsp1 = new JScrollPane(snakeJList);
				gridbag.setConstraints(jsp1, constraint);
				add(jsp1);
				
				
			constraint.gridx = 3;
			constraint.anchor = EAST;
				
				brainJList = new JList();
				brainJList.addMouseListener(new BrainMouseListener());
				
				JScrollPane jsp2 = new JScrollPane(brainJList);
				gridbag.setConstraints(jsp2, constraint);
				add(jsp2);
		
		constraint.fill = NONE;
		constraint.gridheight = 1;
		constraint.weightx = 0.0;
		
			constraint.anchor = CENTER;
			constraint.gridx = 2;
			constraint.gridwidth = 1;
			
				constraint.gridy = 3;
				
					addSnakeButton = new JButton("←");
					addSnakeButton.addActionListener(new AddSnakeListener());
					gridbag.setConstraints(addSnakeButton, constraint);
					add(addSnakeButton);
				
				constraint.gridy = 4;
				
					removeSnakeButton = new JButton("→");
					removeSnakeButton.addActionListener(new RemoveSnakeListener());
					gridbag.setConstraints(removeSnakeButton, constraint);
					add(removeSnakeButton);
			
			constraint.anchor = SOUTH;
			constraint.gridx = 0;
			constraint.gridy = 9;
			constraint.gridwidth = 5;
			
				reloadAllBrainsButton = new JButton("Reload all brains");
				reloadAllBrainsButton.addActionListener(new ReloadBrainsListener());
				gridbag.setConstraints(reloadAllBrainsButton, constraint);
				add(reloadAllBrainsButton);
		
		
		/* //~Vanilla version - ugly but works always.
		super(new BorderLayout());
		
		JPanel west = new JPanel(new BorderLayout());
		JPanel east = new JPanel(new BorderLayout());
		JPanel center = new JPanel(new BorderLayout());
		
		add(west, BorderLayout.WEST);
		add(east, BorderLayout.EAST);
		add(center, BorderLayout.CENTER);
		
		snakeJList = new JList();
		snakeJList.addMouseListener(new SnakeMouseListener());
		west.add(new JScrollPane(snakeJList));
		
		brainJList = new JList();
		brainJList.addMouseListener(new BrainMouseListener());
		east.add(new JScrollPane(brainJList));
		
		addSnakeButton = new JButton("←");
		addSnakeButton.addActionListener(new AddSnakeListener());
		center.add(addSnakeButton, BorderLayout.NORTH);
		
		removeSnakeButton = new JButton("→");
		removeSnakeButton.addActionListener(new RemoveSnakeListener());
		center.add(removeSnakeButton, BorderLayout.SOUTH);
		*/
		loadBrains();
	}
	
	private String loadBrains()
	{
		ClassLoader parentClassLoader = MainWindow.class.getClassLoader();
		BotClassLoader classLoader = new BotClassLoader(parentClassLoader);
		
		FilenameFilter filter = new ClassfileFilter();
		File botFolder = new File("./bot");
		File[] listOfFiles = botFolder.listFiles(filter);
		
		String loadedBrains = "";
		for (File file : listOfFiles)
		{
			if (file.isDirectory())
				continue;
			
			String name = file.getName();
			name = name.substring(0, name.lastIndexOf("."));
			
			Class<?> c;
			try
			{
				c = classLoader.getClass(name);
			}
			catch (Throwable e)
			{
				JOptionPane.showMessageDialog(this, e.toString());
				continue;
			}
			
			Class<? extends Brain> brainClass;
			try
			{
				brainClass = c.asSubclass(Brain.class);
			}
			catch (Exception e)
			{
				continue;
			}
			
			loadedBrains += name + '\n';
			brains.put(name, brainClass);
		}
		
		brainJList.setListData(brains.keySet().toArray());
		
		return loadedBrains;
	}
	
	static private class ClassfileFilter implements FilenameFilter
	{
		public boolean accept(File dir, String name)
		{
			name = name.substring(name.lastIndexOf("."), name.length());
			return name.equalsIgnoreCase(".class");
		}
	}

	
		
	private class AddSnakeListener implements ActionListener
	{
		private String generateSnakeName(String name)
		{
			String snakeName = name;
			int numberOfSnakesWithTheSameBrain = 1;
			
			while (snakes.containsKey(snakeName))
			{
				++numberOfSnakesWithTheSameBrain;
				snakeName = name + "#" + numberOfSnakesWithTheSameBrain;
			}
			
			return snakeName;
		}
		
		public void actionPerformed(ActionEvent event)
		{
			Object selectedObject = brainJList.getSelectedValue();
			if (selectedObject == null)
				return;
			
			
			String name = selectedObject.toString();
			
			snakes.put(generateSnakeName(name), name);
				
			snakeJList.setListData(snakes.keySet().toArray());
		}
	}
	
	private class RemoveSnakeListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			Object selectedObject = snakeJList.getSelectedValue();
			if (selectedObject == null)
				return;
			
			snakes.remove(selectedObject.toString());
			
			snakeJList.setListData(snakes.keySet().toArray());
		}
	}
	
	private class ReloadBrainsListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			String reloadedBrains = loadBrains();
			JOptionPane.showMessageDialog(SnakeSettingsPanel.this, "Successfully reloaded:\n" + reloadedBrains);
		}
	}
	
	private class SnakeMouseListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() % 2 == 0)
			{
				new RemoveSnakeListener().actionPerformed(null);
			}
		}
	}
	
	private class BrainMouseListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() % 2 == 0)
			{
				new AddSnakeListener().actionPerformed(null);
			}
		}
	}
	
	
	
	
	public Map<String, Brain> getSnakes() throws Exception
	{
		Map<String, Brain> snakeMap = new TreeMap<String, Brain>();
		for (Map.Entry<String, String> snake : snakes.entrySet())
		{
			Brain brain = brains.get(snake.getValue()).newInstance();
			snakeMap.put(snake.getKey(), brain);
		}
		return snakeMap;
	}
}
