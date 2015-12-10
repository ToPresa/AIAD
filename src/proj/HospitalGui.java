package proj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class HospitalGui extends JFrame {
	private static final long serialVersionUID = 1L;

	private AgenteHospital myAgent;

	private JComboBox combo;

	HospitalGui(AgenteHospital a) {
		super(a.getLocalName());

		setTitle("Estruture o seu Hospital!");

		TitledBorder nameBorder = BorderFactory.createTitledBorder("Escolha as salas disponíveis:");

		myAgent = a;
		combo = new JComboBox((new Object[] {"Oncologia", "Pediatria", "Urgência", "Ortopedia", "Genecologia",
				"Medicina Dentária"}));
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(400, 250));
		p.add(combo);
		p.setBorder(nameBorder);
		getContentPane().add(p);

		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String sala = combo.getSelectedItem().toString();
					myAgent.updateHospital(sala);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(HospitalGui.this, "Invalid values. " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		myAgent.updateHospital("Triagem");
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);

		// Make the agent terminate when the user closes
		// the GUI using the button on the upper right corner
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		});

		setResizable(false);
	}

	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int) screenSize.getWidth() / 2;
		int centerY = (int) screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}

}
