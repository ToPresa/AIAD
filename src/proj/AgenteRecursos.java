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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyStore.Entry;
import java.util.*;

public class AgenteRecursos extends Agent {

	private static final long serialVersionUID = 1L;
	
	AID[] Salas;
	float melhorEstado = 1;
	float escolhido=0;
	AID paciente1;
	float var;
	String teste = "";
	List<Float> estados;
	HashMap<String, Float> queue;
	
	// Put agent initializations here
	protected void setup() {
		paciente1 = new AID();
		queue = new HashMap <String, Float>();
		estados = new ArrayList<Float>();
		// Registar o serviço da sala nas "paginas-amarelas"
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
		
		addBehaviour( new CheckUp()); 
		
		// Add the behaviour serving queries from buyer agents
		//System.out.print(this.getLocalName().toString());
		//if(this.getLocalName().toString() != "Triagem")
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


	private class CheckUp extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		private boolean end = false;
		
		public void action() {
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

					writeFile(conjuntoSintomas[1].toString(), msg.getSender().toString(), estado);
					
					myAgent.send(reply);
					end = true;

				}
			}
			
			else
				block();
		}

	}
	
	
	public static void writeFile(String Sala, String Sender, Float Estado) {
		try {

			String content = Sender + ";" + Estado + "\n";
			String currentDirFile = System.getProperty("user.dir");
		
			System.out.print(currentDirFile + "\\" + "resources" +  "\\" + Sala + ".txt");
			File file = new File(currentDirFile + "\\" + "resources" +  "\\" + Sala + ".txt");
			
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

			System.out.println("Writing Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static StringBuffer Readfile(String Sala, HashMap<String, Float> queue){
		
		StringBuffer stringBuffer = null;
		
		try {
			
			String currentDirFile = System.getProperty("user.dir");
			
			File file = new File(currentDirFile + "\\" + "resources" +  "\\" + Sala + ".txt");

			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			stringBuffer = new StringBuffer();
			String line;
		
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
				
				String[] parts = line.split(";");
				String name = parts[0];
				String estado = parts[1]; 
				//System.out.println("Contents of file 11 :  " + name + "  --  " + estado);
				queue.put(name, Float.parseFloat(estado));
				//System.out.println("Contents of file:  " + line);
				stringBuffer.append("\n");
				
				
			}
			
			fileReader.close();
			//System.out.println("Contents of file:");
			//System.out.println(stringBuffer.toString());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return stringBuffer;
		
		
		
	}


	private class OfferRequestsServer extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		MessageTemplate mt;
		private int step = 0;
		
		public void action() {

			switch (step) {

			case 0:
				StringBuffer stringBuffer = Readfile(myAgent.getLocalName().toString(), queue);
				
				
				//print dos valores da queue
				   Iterator it = queue.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
				        System.out.println(pair.getKey() + " = " + pair.getValue());
				    }
				    
				    //Float min = Collections.min(queue.values());
				    
				    //System.out.printf(" MINIMOOOOOOOOOO " + min);
				    
				System.out.println("CRLLLL :  " + queue.size());
				
				mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null && msg.getConversationId() == "marcar-recurso") {
					// CFP Message received. Process it
					ACLMessage reply = msg.createReply();

					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent("Sala marcada");
					reply.setReplyWith("marcar"+ System.currentTimeMillis());
					step = 1;
						myAgent.send(reply);

					}
				
				 else {
					block();
				}
				
				break;

			case 1:
				
				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage reply2 = myAgent.receive(mt);
				
				if (reply2 != null
						&& reply2.getConversationId() == "marcar-recurso") {
					// Reply received
					if (reply2.getPerformative() == ACLMessage.INFORM) {
						String resposta = reply2.getContent().toString();
						
						System.out.println("Foi selecionado: " + Float.valueOf(resposta));

							paciente1 = reply2.getSender();
							step = 2;
					}
				}

				else {
					block();
				}

				break;

			case 2:
				
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				System.out.print(String.valueOf(queue.size()));
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
					System.out.println("Foi libertada a sala! " + resposta);
					step = 0;
					System.out.print(queue.size());
					queue.remove(theend.getSender());
					System.out.print(var);
				}

				else {
					block();
					step = 3;
				}
				
				break;
			}
		}
	} // End of inner class OfferRequestsServer


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
