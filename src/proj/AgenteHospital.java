 package proj;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class AgenteHospital extends Agent {
		private static final long serialVersionUID = 1L;
		private HospitalGui myGui;
		//private RecursosGui myGui2;
				
		// Put agent initializations here
		protected void setup() {	
			
			// Create and show the GUI
			myGui = new HospitalGui(this);
			myGui.showGui();
			
			//myGui2 = new RecursosGui();				
		}
				
		public void updateHospital(final String sala) {
			addBehaviour(new OneShotBehaviour() {
				private static final long serialVersionUID = 1L;

				public void action() {
					
					ContainerController cc = getContainerController();
					AgentController ac = null;
					
					try {
						ac = cc.createNewAgent(sala, "proj.AgenteRecursos" , null);
						ac.start();
					} catch (StaleProxyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
					
					//list.adiciona(sala);
					System.out.println(sala + " adicionada!");
				}
			});
		}
		
		// Put agent clean-up operations here
		protected void takeDown() {
			// Deregister from the yellow pages
			try {
				DFService.deregister(this);
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
	
			// Close the GUI
			myGui.dispose();
						
			// Printout a dismissal message
			System.out.println("Hospital " + getAID().getLocalName() + " fechou!");
		}

}