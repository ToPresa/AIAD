package proj;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class RecursosGui extends JFrame{

	private static final long serialVersionUID = 1L;

	
		private PacientesButtonHandler pbHandler;
	    private RecursosButtonHandler ebHandler;
		
	    public RecursosGui() {
	        EventQueue.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	                } catch (ClassNotFoundException ex) {
	                } catch (InstantiationException ex) {
	                } catch (IllegalAccessException ex) {
	                } catch (UnsupportedLookAndFeelException ex) {
	                }

	                JFrame frame = new JFrame();
	                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                frame.setLayout(new BorderLayout());
	                frame.add(new MenuPanel());
	            
	                frame.pack();
	                frame.setLocationRelativeTo(null);
	                frame.setVisible(true);
	            }            
	        });
	    }

	    protected class MenuPanel extends JPanel {

	        public MenuPanel() {            
	            JLabel label = new JLabel("Hospital");
	            label.setFont(new Font("Arial" ,Font.BOLD, 16));
	            setLayout(new GridBagLayout());

	    		setPreferredSize(new Dimension(300, 300));
	            	
	            GridBagConstraints gbc = new GridBagConstraints();
	            gbc.gridx = 0;
	            gbc.gridy = 0;
	            add(label, gbc);

	            gbc.fill = GridBagConstraints.HORIZONTAL;
	            gbc.gridy++;
	            JButton Pacientes = new JButton("Pacientes Atendidos");
	            JButton Recursos = new JButton("Recusos Abertos");
	            
	            pbHandler = new PacientesButtonHandler();
		        ebHandler = new RecursosButtonHandler();
	            
		        Pacientes.addActionListener(pbHandler);
		        Recursos.addActionListener(ebHandler);
		        		        
	            add(Pacientes, gbc);
	            gbc.gridy++;
	            add(Recursos, gbc);
	        }
	    }
	    
	    
	    public class PacientesButtonHandler implements ActionListener
	    {
	        public void actionPerformed(ActionEvent e)
	        {
	        	
	        	dispose();
	        	new DynamicJList().setVisible(true);
	        }
	    }
	    
	    public class RecursosButtonHandler implements ActionListener
	    {
	        public void actionPerformed(ActionEvent e)
	        {
	        	System.exit(0);

	        }
	    }
	 
	}
