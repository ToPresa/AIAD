 package proj;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.nio.file.Files;
import java.util.*;

public class AgenteHospital extends Agent {
	
		private HospitalGui myGui;
		
		// Put agent initializations here
		protected void setup() {
			// Create and show the GUI
			myGui = new HospitalGui(this);
			myGui.showGui();
				
		}
		
		
		public void updateHospital(final String sala) {
			addBehaviour(new OneShotBehaviour() {
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