package proj;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgentePaciente extends Agent{
	
	private String sintoma;
	private float estado;
	private AID[] recursos;
		
	protected void setup() {
		// Printout a welcome message
		System.out.println("Bem Vindo Sr/Sra: "+ getAID().getLocalName() + "!");

		// Obter os sintomas de um paciente como argumento de entrada
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			sintoma = (String) args[0];
			System.out.println("Sintoma registado: " + sintoma);
			//recebo os sintomas
			//depois de analisar, verifico que o paciente quer a sala X
			
			//sintoma = SalaPaciente(sintoma);
						
			// Schedules a request to Hospital agents every 10s
			addBehaviour(new TickerBehaviour(this, 5000) {
				protected void onTick() {
					System.out.println("A tentar curar: " + sintoma);
					// Update the list of seller agents
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("alocar-recursos");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("Encontrei estes recursos:");
						recursos = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							recursos[i] = result[i].getName();
							System.out.println(recursos[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// Perform the request
					myAgent.addBehaviour(new RequestCheckUp());
					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
		}
		else {
			// Make the agent terminate
			System.out.println("N�o especificou um sintoma!");
			doDelete();
		}
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Adeus " + getAID() + "!");
	}
	
	private class RequestCheckUp extends Behaviour {
		
		private AID recurso; // The agent who provides the best offer
		private int repliesCnt = 0; // The counter of replies from Hospital agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
		boolean done = false;
		
		public void action() {
			
			switch (step) {
			case 0:
				// Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < recursos.length; ++i) {
					cfp.addReceiver(recursos[i]);
				} 
				cfp.setContent(sintoma);
				cfp.setConversationId("marcar-checkup");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("marcar-checkup"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
				
			case 1:
				// Receive all proposals/refusals from Hospital agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// This is an offer 
						
						estado = Float.parseFloat(reply.getContent().toString());
						System.out.println("O seu estado de sa�de Sr/Sra: " + myAgent.getName() + "foi avaliado em" + estado);
						done = true;
					}
				}
				
				else {
					block();
				}
				
				break;

			}        
	}

		@Override
		public boolean done() {
			if(done && step == 1)
				return true;
			else{
			System.out.println("N�o foi possivel avaliar o seu estado de saude!");
			return false;
			}
		}
	}
	
	private class RequestPerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
		private AID recurso; // The agent who provides the best offer
		private int repliesCnt = 0; // The counter of replies from Hospital agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < recursos.length; ++i) {
					cfp.addReceiver(recursos[i]);
				} 
				cfp.setContent(sintoma);
				cfp.setConversationId("marcar-recurso");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("marcar-recurso"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from Hospital agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer 
						String resposta = reply.getContent().toString();
						//System.out.println("RESPOSTA: "+resposta+ " "+ sintoma + "lllll " + reply.getSender().toString());
						
						// This is the best offer at present
						recurso = reply.getSender();
		
					}
					repliesCnt++;
					if (repliesCnt >= recursos.length) {
						// We received all replies
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2:
				// Send the purchase order to the seller that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(recurso);
				order.setContent(sintoma);
				order.setConversationId("marcar-recurso");
				order.setReplyWith("marcar"+System.currentTimeMillis());
				myAgent.send(order);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("marcar-recurso"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:      
				// Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						System.out.println(sintoma+" tratado com sucesso!");
						
						System.out.println("Sintoma: " + sintoma);
						myAgent.doDelete();
					}
					else {
						System.out.println("N�o foi possivel tratar o seu sintoam!");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
			}        
		}

		public boolean done() {
			if (step == 2 && recurso == null) {
				System.out.println("N�o � possivel tratar: "+sintoma);
			}
			return ((step == 2 && recurso == null) || step == 4);
		}
	}  // End of inner class RequestPerformer

}
