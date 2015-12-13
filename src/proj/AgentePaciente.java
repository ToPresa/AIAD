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

	private static final long serialVersionUID = 1L;
	
	private List<String> sintoma;
	private float estado = 0;
	private AID[] recursos;
	private List<String> procura;
	String temp = "";

	protected void setup() {
		// Printout a welcome message
		System.out.println("Bem Vindo Sr/Sra: " + getAID().getLocalName() + "!");
		
		sintoma = new ArrayList<String>();
		procura = new ArrayList<String>();
		
		// Obter os sintomas de um paciente como argumento de entrada
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			for(int n = 0; n < args.length; ++n){
			sintoma.add(args[n].toString());
			System.out.println("Sintoma registado: " + args[n].toString());
			}
			
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("alocar-pacientes");
			sd.setName("Doentes");
			dfd.addServices(sd);
			
			try {
				DFService.register(this, dfd);
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
			

			addSala(sintoma);
			
			// Schedules a request to Hospital agents every 10s
			addBehaviour(new TickerBehaviour(this, 5000) {
				private static final long serialVersionUID = 1L;

				protected void onTick() {
					System.out.println("A tentar curar: "+ myAgent.getLocalName());
					// Update the list of seller agents
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("alocar-recursos");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent,
								template);
						//System.out.println("Encontrei estes recursos:");
						recursos = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							recursos[i] = result[i].getName();
							//System.out.println(recursos[i].getName());
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
		private int step = 0;

		public void action() {

			switch (step) {
			case 0:
				//Solicitar o checkup à sala de Triagem
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				for (int i = 0; i < recursos.length; ++i) {
					if(recursos[i].getLocalName().toString().matches("Triagem"))
						cfp.addReceiver(recursos[i]);
						
				}
				cfp.setContent(sintoma.toString() + ";" + procura.toString());
				cfp.setConversationId("marcar-checkup");
				cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique
																		// value
				myAgent.send(cfp);

				step = 1;
				break;

			case 1:
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Receber resposta da triagem que diz respeito ao meu estado de saúde
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage reply = myAgent.receive(mt);

				if (reply != null && reply.getConversationId() == "marcar-checkup") {
					// Reply received
						estado = Float.parseFloat(reply.getContent().toString());

						System.out.println("O seu estado de saúde Sr/Sra: "
								+ myAgent.getLocalName() + " foi avaliado em "
								+ estado);
						
				}

				else {
					block();
				}

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
		private int step = 0;

		public void action() {
			switch (step) {
			
			case 0:
				step = 0;
				// Receive all proposals/refusals from Hospital agents
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage reply = myAgent.receive(mt);
				
				ACLMessage leiloar = new ACLMessage(ACLMessage.INFORM);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM && reply.getConversationId() == "marcar-recurso") {

						leiloar.addReceiver(reply.getSender());
						
						leiloar.setContent("Atendido");
						leiloar.setConversationId("alocar-recurso");
						leiloar.setReplyWith("alocar"
								+ System.currentTimeMillis());
						leiloar.setContent(String.valueOf(estado));
						
						myAgent.send(leiloar);
						step = 1;

					}

				}		
				else {
					block();
				}

				break;
				
			case 1:
				// Receive the purchase order reply
				//System.out.println("Case 1");
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				
				ACLMessage abresala = new ACLMessage(ACLMessage.INFORM);
				ACLMessage reply2 = myAgent.receive(mt2);

				if (reply2 != null) {

					// Purchase order reply received
					if (reply2.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
							&& reply2.getConversationId() == "concluir") {
						
						String estadoos="";
						estadoos = reply2.getContent();
						String[] estados = estadoos.split(";");
						
						estado = Float.valueOf(estados[0]); 
						
						//System.out.println("ESTADO: " + String.valueOf(estado) + estados[1]);
						if(Float.valueOf(estado).equals(0.0f)){
							System.out.println("O meu estado atingiu 0, morri! :,(");
							procura.clear();
						}
						else if(Float.valueOf(estado).equals(1.0f)){
							System.out.println("Estou totalmente curado! :)");
							procura.clear();
						}
						else{
							System.out.println(sintoma.get(0).toString() + " tratado com sucesso!");
							
							if(estados[1].equals("melhorou")){
								
							int l = procura.indexOf(reply2.getSender().getLocalName());
							//System.out.println("LLLL CRLLLL:   " + l);
							if(l>=0)
								procura.remove(l);
							}
							}
							
							abresala.addReceiver(reply2.getSender());
							abresala.setContent(Float.toString(estado));
							abresala.setConversationId("terminar-paciente");
							abresala.setReplyWith("terminar"
									+ System.currentTimeMillis());
													
							myAgent.send(abresala);
							
							if(Thread.currentThread().getState() != Thread.State.BLOCKED)
								step = 2;
				
					
					} else {
						System.out
								.println("Não foi possivel tratar o seu sintoma!");
						step = 0;
					}

				} else {
					block();
				}
				break;

			}
		}

		public boolean done() {
			if (step == 2 && procura.size() == 0) {
				myAgent.doDelete();
				return true;
			} else
				return false;
		}
	} // End of inner class RequestPerformer
	
	public void addSala(List<String> sintoma){

		for(int n = 0; n < sintoma.size();++n){
			
		switch(sintoma.get(n)){
		
		case "cancro":
			
			procura.add("Oncologia");
			break;
		
		case "febre":
			
			procura.add("Pediatria");
			break;
			
		case "calos":
			procura.add("Ortopedia");
			break;
			
		case "baleado":
			procura.add("Urgência");
		break;
		
		case "gravidez":
			procura.add("Genecologia");
		break;
		
		case "caries":
			procura.add("Medicina Dentária");
		break;
		
		}
		}
		
	}

}

