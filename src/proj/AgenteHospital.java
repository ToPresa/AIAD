package proj;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class AgenteHospital extends Agent {

	private HospitalGui myGui;
	List<Sala> listaSala;
	float melhorEstado = 1;
	AID paciente1;

	// Put agent initializations here
	protected void setup() {
		// Create the catalogue
		// lista = new List();
		listaSala = new ArrayList<Sala>();
		paciente1 = new AID();

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
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new CheckUp());

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
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Close the GUI
		myGui.dispose();
		// Printout a dismissal message
		System.out.println("Hospital " + getAID().getLocalName() + " fechou!");
	}

	public void updateHospital(final String sala) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				// sintoma vao ser salas/recursos
				Sala criada = new Sala(sala);
				System.out.println("SALA ABERTA: " + sala);
				criada.setSintomas(sala);
				listaSala.add(criada);
				System.out.println(sala + " adicionada!");
			}
		});
	}

		private class CheckUp extends Behaviour {

			private static final long serialVersionUID = 1L;
			
			public void action() {
				
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					//System.out.print(msg.getSender().toString());
					// CFP Message received. Process it
					String sintoma = msg.getContent();
					ACLMessage reply = msg.createReply();
					
					String[] conjuntoSintomas = sintoma.split(";");
					
				if(msg.getConversationId() == "marcar-checkup"){
					float estado = checkUp(conjuntoSintomas);
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Float.toString(estado));
					reply.setReplyWith("marcar"+System.currentTimeMillis());
					myAgent.send(reply);

				}
			}
		
		}

			@Override
			public boolean done() {
				return false;
			}
	}
	
	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String sintoma = msg.getContent();
				ACLMessage reply = msg.createReply();
				boolean encontrou = false;
				
				String[] conjuntoSintomas = sintoma.split(";");

				if(msg.getConversationId() == "marcar-recurso"){
					for (int i = 0; i < listaSala.size(); i++) {
						for (int j = 0; j < conjuntoSintomas.length; j++) {
							if (listaSala.get(i).getSintomas().contains(conjuntoSintomas[j]) && listaSala.get(i).ocupada == false) {
								// Existe uma sala que pode tratar aquele sintoma disponivel. Reply
								
								encontrou = true;
								reply.setPerformative(ACLMessage.PROPOSE);
								reply.setContent(Integer.toString(i));
								reply.setReplyWith("marcar"+System.currentTimeMillis());
								break;
							}
						}
					}

					if (!encontrou) {
						// The requested book is NOT available for sale.
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("not-available");
					}
					
					//System.out.println("Passei no f");
					myAgent.send(reply);
				
				}
				
				MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				MessageTemplate mt3 = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
				ACLMessage reply2 = myAgent.receive(mt2);
				ACLMessage tryhard = myAgent.receive(mt3);
			
				if (reply2 != null && reply2.getConversationId() == "marcar-recurso") {
					// Reply received
					if (reply2.getPerformative() == ACLMessage.INFORM) {
						// This is an offer 
						String resposta = reply2.getContent().toString();
						String[] resposta3 = resposta.split(";");
						
						if(Float.parseFloat(resposta3[1]) < melhorEstado){
							melhorEstado = Float.parseFloat(resposta3[1]);
							paciente1 = reply2.getSender();
						}
						listaSala.get(Integer.valueOf(resposta3[0])).setOcupada(true);
						System.out.println("Foi selecionado: "+ Float.parseFloat(resposta3[1]) + "  " + melhorEstado + "sala: " + Integer.valueOf(resposta3[0]));					
						melhorEstado = 1;
					}
				}
				
				else if (tryhard != null && tryhard.getConversationId() == "abrirsala") {

							String resposta = tryhard.getContent().toString();

							listaSala.get(Integer.valueOf(resposta)).setOcupada(false);
							System.out.println("Foi libertada a sala! " + resposta);
				}
				

			} else {
				block();
			}
		}
	} // End of inner class OfferRequestsServer

	private class PurchaseOrdersServer extends Behaviour {
		public void action() {
			
			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			order.addReceiver(paciente1);
			order.setContent("accepted");
			order.setConversationId("concluiratendimento");
			order.setReplyWith("accepted"+System.currentTimeMillis());
			myAgent.send(order);
			
		}

		@Override
		public boolean done() {
			return false;
		}
	} // End of in

	public List<Sala> geLlistaSala() {
		return listaSala;
	}
	
	public float checkUp(String[] sintomas){
		
		float estado=1;
		
		for(int i = 0; i < sintomas.length; i++){
			//System.out.println("CRL KA : " + sintomas[i] );
			switch(sintomas[i]){
			
			case "o":
				estado -= Math.random();
				//System.out.println("HEIN : " + estado);
				break;
			case "u":
				estado -=0.1;
				//System.out.println("HEIN222 : " + estado);
				break;
			case "p":
				estado -=0.2;
				//System.out.println("HEIN3333: " + estado);
				break;
			case "or":
				estado -=0.4;
				//System.out.println("HEIN44 44: " + estado);
				break;
			default:
				break;
			}
			
		}
		
		return estado;
		
	}

}
