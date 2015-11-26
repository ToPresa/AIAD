package proj;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class AgenteHospital extends Agent{
	
	private HospitalGui myGui;
	
	List<Sala> listaSala;
	
	// Put agent initializations here
	protected void setup() {
		// Create the catalogue
		//lista = new List();
		listaSala = new ArrayList<Sala>();


		// Create and show the GUI 
		myGui = new HospitalGui(this);
		myGui.showGui();

		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("alocar-recursos");
		sd.setName("Serviço Hospitalar");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behaviour serving queries from buyer agents
		addBehaviour(new OfferRequestsServer());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new PurchaseOrdersServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Close the GUI
		myGui.dispose();
		// Printout a dismissal message
		System.out.println("Hospital "+getAID().getLocalName()+" fechou!");
	}
	
	public void updateCatalogue(final String sintoma) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				//sintoma vao ser salas/recursos
				Sala criada = new Sala(sintoma);
				criada.Sintomas.add("pernas");
				listaSala.add(criada);
				System.out.println(sintoma + "adicionado!");
			}
		} );
	}
	
	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String sintoma = msg.getContent();
				ACLMessage reply = msg.createReply();
				
				for(int i=0; i < listaSala.size(); i++) {
				if (listaSala.get(i).getSintomas().contains(sintoma) && listaSala.get(i).ocupada == false) {
					// The requested book is available for sale. Reply with the price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(listaSala.get(i).getNome());
				}
				else {
					// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	
	private class PurchaseOrdersServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String sintoma = msg.getContent();
				ACLMessage reply = msg.createReply();

				for (int i = 0; i < listaSala.size(); i++) {
					if (listaSala.get(i).getSintomas().contains(sintoma)) {
						reply.setPerformative(ACLMessage.INFORM);
						listaSala.get(i).ocupada=true;
						
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						listaSala.get(i).ocupada=false;
						System.out.println(sintoma + " tratado ao " + msg.getSender().getName());
					} else {
						// The requested book has been sold to another buyer in
						// the meanwhile .
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("not-available");
					}
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End of in
	

	public List<Sala> geLlistaSala() {
		return listaSala;
	}	


}
