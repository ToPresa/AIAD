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

public class AgentePaciente extends Agent {

	private String sintoma;
	private float estado = 0;
	private AID[] recursos;
	String temp = "";

	protected void setup() {
		// Printout a welcome message
		System.out
				.println("Bem Vindo Sr/Sra: " + getAID().getLocalName() + "!");

		// Obter os sintomas de um paciente como argumento de entrada
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			sintoma = (String) args[0];
			System.out.println("Sintoma registado: " + sintoma);
			// recebo os sintomas
			// depois de analisar, verifico que o paciente quer a sala X

			// sintoma = SalaPaciente(sintoma);

			// Schedules a request to Hospital agents every 10s
			addBehaviour(new TickerBehaviour(this, 5000) {
				protected void onTick() {
					System.out.println("A tentar curar: "
							+ myAgent.getLocalName());
					// Update the list of seller agents
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("alocar-recursos");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent,
								template);
						System.out.println("Encontrei estes recursos:");
						recursos = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							recursos[i] = result[i].getName();
							System.out.println(recursos[0].getName());
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// Perform the request
					if(estado == 0)
						myAgent.addBehaviour(new RequestCheckUp());

						myAgent.addBehaviour(new RequestPerformer());
				}
			});
		} else {
			// Make the agent terminate
			System.out.println("Não especificou um sintoma!");
			doDelete();
		}
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Adeus " + getAID() + "!");
	}

	private class RequestCheckUp extends Behaviour {

		private static final long serialVersionUID = 1L;
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
		boolean done = false;

		public void action() {

			switch (step) {
			case 0:
				System.out.println("CHECKUPPPP");
				// Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				for (int i = 0; i < recursos.length; ++i) {
					cfp.addReceiver(recursos[i]);
				}
				cfp.setContent(sintoma);
				cfp.setConversationId("marcar-checkup");
				cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique
																		// value
				myAgent.send(cfp);

				mt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("marcar-checkup"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));

				step = 1;
				break;

			case 1:
				System.out.println("CHECKUPPPP2");
				// Receive all proposals/refusals from Hospital agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM
							&& reply.getConversationId() == "marcar-checkup") {
						// This is an offer

						estado = Float
								.parseFloat(reply.getContent().toString());

						System.out.println("O seu estado de saúde Sr/Sra: "
								+ myAgent.getLocalName() + " foi avaliado em "
								+ estado);
						done = true;
					}
				}

				else {
					block();
				}

				step = 0;
				break;

			}
		}

		@Override
		public boolean done() {
			return (estado != 0);
		}
	}

	private class RequestPerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
		private int repliesCnt = 0; // The counter of replies from Hospital
									// agents
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
				cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique
																		// value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("marcar-recurso"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case 1:
				// Receive all proposals/refusals from Hospital agents
				ACLMessage reply = myAgent.receive(mt);
				ACLMessage leiloar = new ACLMessage(ACLMessage.INFORM);
								
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {

						leiloar.addReceiver(reply.getSender());
						temp = reply.getContent();
						leiloar.setContent(Integer.valueOf(reply.getContent())
								+ ";" + Float.toString(estado));
						leiloar.setConversationId("marcar-recurso");
						leiloar.setReplyWith("marcar"
								+ System.currentTimeMillis());
						
						myAgent.send(leiloar);
						step = 2;

					}

					else if (reply.getPerformative() == ACLMessage.REFUSE) {
						step = 0;
						break;
					}

				}		
				else {
					block();
				}

				break;
			case 2:
				// Receive the purchase order reply
				MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				
				ACLMessage abresala = new ACLMessage(ACLMessage.INFORM);
				ACLMessage reply2 = myAgent.receive(mt2);

				if (reply2 != null) {

					// Purchase order reply received
					if (reply2.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
							&& reply2.getConversationId() == "concluir") {

						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						System.out.println(sintoma + " tratado com sucesso!");
						// This is the best offer at present
						abresala.addReceiver(reply2.getSender());
						abresala.setContent(temp);
						abresala.setConversationId("terminar-paciente");
						abresala.setReplyWith("terminar"
								+ System.currentTimeMillis());
												
						myAgent.send(abresala);
						
						step = 3;

					} else {
						System.out
								.println("Não foi possivel tratar o seu sintoma!");
						step = 0;
					}

				} else {
					block();
				}
				break;
				
			case 3:
				break;

			}
		}

		public boolean done() {
			if (step == 3) {
				myAgent.doDelete();
				return true;
			} else
				return false;
		}
	} // End of inner class RequestPerformer

}
