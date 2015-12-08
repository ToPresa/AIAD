package proj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class DynamicJList extends JFrame {
	private static final long serialVersionUID = 1L;
	
	JMenuBar mbar;
	JMenu menu;
	JTextField jf;
	JButton jb;
	JList<String> jl;
	Vector<String> v;

	public DynamicJList() {
		// Set frame properties
		
		setSize(300, 300);
		setLayout(new FlowLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);

		// Create a vector that can store String objects
		v = new Vector<String>();

		// Create a JList that is capable of storing String type items
		jl = new JList<String>(v);

		// Make it fat.
		jl.setPreferredSize(new Dimension(300, 300));

		// Add key listener to JList jl
		jl.addKeyListener(new KeyAdapter() {

			// What to do when a key is pressed?
			public void keyPressed(KeyEvent ke) {

				// If user presses Delete key,
				if (ke.getKeyCode() == KeyEvent.VK_DELETE) {

					// Remove the selected item
					v.remove(jl.getSelectedValue());

					// Now set the updated vector (updated items)
					jl.setListData(v);

				}
			}
		});

		// Create a new MenuBar
		mbar = new JMenuBar();


		// Create a JButton with text Add
		jb = new JButton("Add");

		jf = new JTextField(20);

		// Add key listener to JTextField
		jf.addKeyListener(new KeyAdapter() {

			// What to do when a key is pressed?
			public void keyPressed(KeyEvent ke) {
				// Make the JButton enabled
				jb.setEnabled(true);

				// When user presses enter key, then..
				if (ke.getKeyCode() == KeyEvent.VK_ENTER)
					// Click on JButton, doClick() does it!
					jb.doClick();

			}
		});

		// Create a scrollpane for JList jl
		JScrollPane js = new JScrollPane(jl);

		// Add that scrollpane to JFrame. You don't need to add JList again, as
		// you add scrollpane, it means that you are adding JList because
		// JScrollPane is pointing JList only!
		add(js);

	}
	
	public void adiciona(String s) {

		// Add what the user types in JTextField jf, to the vector
		v.add(s);

		// Now set the updated vector to JList jl
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				jl.setListData(v);
			}
		});
		

		// Make the button disabled
		jb.setEnabled(false);

	}
}
