package proj;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import jade.util.leap.List;

public class GuiHospital extends JFrame {

	JPanel panel = new JPanel();
	private AgenteHospital myAgent;
	
	public GuiHospital() {
		setSize(600, 600);
		setTitle("Simulation");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		getContentPane().add(panel);
		dispose();
		setVisible(true);
	}
	
	
	public void paint(Graphics g) {
		super.paint(g); // fixes the immediate problem.
		Graphics2D g2 = (Graphics2D) g;
		
	
				g2.setColor(Color.blue);
				//Line2D line = new Line2D.Double(10 * (i + 1), 10 * (i + 1), 100 * (i + 1), 100 * (i + 1));
				/*
				 * g2.setColor(Color.blue); g2.setStroke(new BasicStroke(10));
				 * g2.draw(line);
				 */
				//g2.draw(line);
	
		 //g.drawRect(10,10,750,750);  
		 //g.setColor(Color.GREEN);  
		 //g.fillRect(10,10,750,750);  
	
	}

	
}
