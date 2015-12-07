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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
		
		String currentDirFile = System.getProperty("user.dir");
		
		File file = new File(currentDirFile + "\\" + "resources" +  "\\" + this.getLocalName() + ".txt");
		
		
		// if file doesnt exists, then create it
		if (!file.exists() && !this.getLocalName().equals("Triagem")) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
			
		// Add the behaviour serving queries from buyer agents
			
			if(!this.getLocalName().toString().equals("Triagem"))
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
		
		String currentDirFile = System.getProperty("user.dir");
		File file = new File(currentDirFile + "\\" + "resources" +  "\\" + getAID().getLocalName() + ".txt");
		
		if (file.exists()) {
			file.delete();
		}
		
		System.out.println("Sala " + getAID().getLocalName() + " fechou!");
	}


	private class CheckUp extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		
		public void action() {
			MessageTemplate mt = MessageTemplate
					.MatchPerformative(ACLMessage.REQUEST);
			
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				// CFP Message received. Process it
				String sintoma = msg.getContent();
				ACLMessage info = msg.createReply();

				String[] conjuntoSintomas = sintoma.split(";");

				if (msg.getConversationId() == "marcar-checkup") {
					
					float estado = checkUp(conjuntoSintomas);
					info.setPerformative(ACLMessage.INFORM);
					info.setContent(Float.toString(estado));
					
					writeFile(conjuntoSintomas[1].toString(), msg.getSender().toString(), estado);
					
					myAgent.send(info);

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
				
				//String[] name2 = name.split("@");
				//String name22 = name2[0];
				//System.out.println("NOME CRL:  " + name22.substring(25, name22.length()).trim());
				//System.out.println("Contents of file 11 :  " + name + "  --  " + estado);
				//queue.put(name22.substring(25, name22.length()).trim(), Float.parseFloat(estado));
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

	public static String getKeyFromValue(HashMap<String,Float> hm, Float value) {
	    for (String o : hm.keySet()) {
	      if (hm.get(o).equals(value)) {
	        return o;
	      }
	    }
	    return null;
	  }

	public static void removerDoFicheiro(String lineToRemove, String Sala) {	
			
		  try {
			  

			  StringBuffer stringBuffer = null;
					
			  String currentDirFile = System.getProperty("user.dir");
			  File inFile = new File(currentDirFile + "\\" + "resources" +  "\\" + Sala + ".txt");


		      if (!inFile.isFile()) {
		        System.out.println("Parameter is not an existing file");
		        return;
		      }

		      //Construct the new file that will later be renamed to the original filename.
		      File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

		      BufferedReader br = new BufferedReader(new FileReader(currentDirFile + "\\" + "resources" +  "\\" + Sala + ".txt"));
		      PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

		      String line = null;

		      //Read from the original file and write to the new
		      //unless content matches data to be removed.
		      while ((line = br.readLine()) != null) {

		        if (!line.substring(25, 25+lineToRemove.length()).trim().equals(lineToRemove)) {
		        	
		        	pw.println(line);
		        	pw.flush();
		        }
		      }
		      pw.close();
		      br.close();

		      //Delete the original file
		      if (!inFile.delete()) {
		        System.out.println("Could not delete file");
		        return;
		      }

		      //Rename the new file to the filename the original file had.
		      if (!tempFile.renameTo(inFile))
		        System.out.println("Could not rename file");

		    }
		    catch (FileNotFoundException ex) {
		      ex.printStackTrace();
		    }
		    catch (IOException ex) {
		      ex.printStackTrace();
		    }
	
		
	}
	
	private class OfferRequestsServer extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		MessageTemplate mt;
		private int step = 0;
		StringBuffer stringBuffer;
		Float min = 1f;
		String key = "";
		AID pac = null;
		
		public void action() {

			switch (step) {

			case 0:
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				boolean advance = false;
				ACLMessage marcaconsulta = new ACLMessage(ACLMessage.INFORM);
				stringBuffer = Readfile(myAgent.getLocalName().toString(), queue);
				//System.out.println("QUEUEEE IN: " + queue.size());
				
				// escolhe o valor minimo
				if (stringBuffer.length() != 0) {
					min = Collections.min(queue.values());
					key = getKeyFromValue(queue, min);
					//System.out.println("CRLLLLL          " + min + " ---" + key);
				}
				
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("alocar-pacientes");
				template.addServices(sd);
				
				try {
					DFAgentDescription[] result = DFService.search(myAgent,
							template);
					//System.out.println("Encontrei estes recursos:");
					for (int i = 0; i < result.length; ++i) {
						if(result[i].getName().toString().equals(key)){
							pac = result[i].getName();
							System.out.println("OH CRL: " + pac.toString());
							advance = true;
						}
							
					}
					
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}

				
				marcaconsulta.addReceiver(pac);
				marcaconsulta.setContent("consulta-marcada");
				marcaconsulta.setConversationId("marcar-recurso");
				marcaconsulta.setReplyWith("marcar" + System.currentTimeMillis());
				
				myAgent.send(marcaconsulta);
				
				if(advance)
					step = 1;
				break;

			case 1:
				
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage reply = myAgent.receive(mt);
				
				if(reply != null){
					if (reply.getPerformative() == ACLMessage.INFORM && reply.getConversationId() == "alocar-recurso") {
				
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				System.out.print("Passei no 1");
				order.addReceiver(pac);
				order.setContent("concluido");
				order.setConversationId("concluir");
				order.setReplyWith("concluir" + System.currentTimeMillis());
				
				myAgent.send(order);
				
				step = 2;
				
					}
				}
				
				else
					block();
				
				break;
				
				
			case 2:
				
				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage theend = myAgent.receive(mt);

				if (theend != null && theend.getConversationId() == "terminar-paciente") {
					
					String resposta = theend.getContent().toString();
					System.out.println("Foi libertada a sala! " + resposta + " --- " + pac.getLocalName() + "   ---  " + myAgent.getLocalName().toString());
					removerDoFicheiro(pac.getName().toString(), myAgent.getLocalName().toString());
					queue.values().remove(Float.valueOf(theend.getContent().toString()));
					//System.out.println("QUEUEEE FI: " + queue.size() + " -- " + pac.getName().toString());
					step = 0;
				}

				else {
					block();
					
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
