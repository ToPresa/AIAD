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

public class AgenteRecursos extends Agent {

	List<Sala> listaSala;
	AID[] Salas;
	float melhorEstado = 1;
	float escolhido=0;
	AID paciente1;
	int index = 0;
	List<Float> estados;
	
	// Put agent initializations here
	protected void setup() {
		// Create the catalogue
		// lista = new List();
		listaSala = new ArrayList<Sala>();
		estados = new ArrayList<Float>();
		paciente1 = new AID();


		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		System.out.print(getAID());
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

	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Sala " + getAID().getLocalName() + " fechou!");
	}


	private class CheckUp extends Behaviour {

		private static final long serialVersionUID = 1L;
		private int done = 0;
		public void action() {
			//System.out.print("CRLLLLLLLL");
			MessageTemplate mt = MessageTemplate
					.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {
				// CFP Message received. Process it
				String sintoma = msg.getContent();
				ACLMessage reply = msg.createReply();

				String[] conjuntoSintomas = sintoma.split(";");

				if (msg.getConversationId() == "marcar-checkup") {
					float estado = checkUp(conjuntoSintomas);
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Float.toString(estado));
					reply.setReplyWith("marcar" + System.currentTimeMillis());
					estados.add(estado);
					myAgent.send(reply);
					done++;

				}
			}

		}
		
		@Override
		public boolean done() {
			return (done == 3);
		}
	}
	
	public float MelhorEstado() {
		melhorEstado = 1;
		for (int i = 0; i < estados.size(); i++) {
			System.out.print(" CRL PO ARRAY: " + estados.get(i));
			if (estados.get(i) < melhorEstado) {
				melhorEstado = estados.get(i);
			}
		}
		return melhorEstado;
	}

	private class OfferRequestsServer extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		MessageTemplate mt;
		private int step = 0;

		public void action() {

			switch (step) {

			case 0:
				mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				
				ACLMessage msg = myAgent.receive(mt);
				System.out.println("1111111selCASSSSSSSSSSSSSSSSS");
				if (msg != null) {
					// CFP Message received. Process it
					String sintoma = msg.getContent();
					ACLMessage reply = msg.createReply();
					boolean encontrou = false;

					String[] conjuntoSintomas = sintoma.split(";");

					if (msg.getConversationId() == "marcar-recurso") {
						for (int i = 0; i < listaSala.size(); i++) {
							for (int j = 0; j < conjuntoSintomas.length; j++) {
								
															
								if (listaSala.get(i).getSintomas()
										.contains(conjuntoSintomas[j])
										&& listaSala.get(i).ocupada == false) {
									// Existe uma sala que pode tratar aquele
									// sintoma disponivel. Reply

									encontrou = true;
									reply.setPerformative(ACLMessage.PROPOSE);
									reply.setContent(Integer.toString(i));
									reply.setReplyWith("marcar"
											+ System.currentTimeMillis());
									step = 1;
									break;
								}
							}
						}

						if (!encontrou) {
							// The requested book is NOT available for sale
							reply.setPerformative(ACLMessage.REFUSE);
							reply.setContent("not-available");
						}

						myAgent.send(reply);

					}
				} else {
					block();
				}

				break;

			case 1:
				System.out.println("Foi selCASSSSSSSSSSSSSSSSS");
				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage reply2 = myAgent.receive(mt);
				if (reply2 != null
						&& reply2.getConversationId() == "marcar-recurso") {
					// Reply received
					if (reply2.getPerformative() == ACLMessage.INFORM) {
						// This is an offer
						String resposta = reply2.getContent().toString();
						String[] resposta3 = resposta.split(";");
						float escolhido = MelhorEstado();
						
						System.out.println("Foi selecionado: "
								+ Float.parseFloat(resposta3[1]) + "  "
								+ escolhido + "sala: "
								+ Integer.valueOf(resposta3[0]));
						
						if (Float.parseFloat(resposta3[1]) == escolhido) {
							//melhorEstado = Float.parseFloat(resposta3[1]);
							paciente1 = reply2.getSender();

							listaSala.get(Integer.valueOf(resposta3[0]))
									.setOcupada(true);
						}
						else step=0;


						for(int i=0; i < estados.size(); i++) {
							if(estados.get(i) == Float.parseFloat(resposta3[1]))
								estados.remove(i);
						}
						//melhorEstado = 1;
					}
				}

				else {
					block();
				}

				break;

			case 2:
				
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(paciente1);
				order.setContent("accepted");
				order.setConversationId("concluir");
				order.setReplyWith("accepted" + System.currentTimeMillis());
				myAgent.send(order);
				
				step = 3;
				
				break;
				
				
			case 3:
				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage theend = myAgent.receive(mt);

				if (theend != null) {

					String resposta = theend.getContent().toString();
					listaSala.get(Integer.valueOf(resposta)).setOcupada(false);
					System.out.println("Foi libertada a sala! " + resposta);
					step = 0;
				}

				else {
					block();
					step = 3;
				}
				
				break;
			}
		}
	} // End of inner class OfferRequestsServer

	public List<Sala> geLlistaSala() {
		return listaSala;
	}

	public float checkUp(String[] sintomas) {

		float estado = 1;

		for (int i = 0; i < sintomas.length; i++) {
			switch (sintomas[i]) {

			case "o":
				estado -= Math.random();
				break;
			case "u":
				estado -= 0.1;
				break;
			case "p":
				estado -= 0.2;
				break;
			case "or":
				estado -= 0.4;
				break;
			default:
				break;
			}

		}

		return estado;

	}

}
