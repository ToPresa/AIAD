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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class AgenteRecursos extends Agent {

	private static final long serialVersionUID = 1L;

	AID[] Salas;
	float melhorEstado = 1;
	float escolhido = 0;
	AID paciente1;
	float var;
	String teste = "";
	List<Float> estados;
	HashMap<String, Float> queue, queue2;
	DynamicJList List;
	static List<String> linhas;

	// Put agent initializations here
	protected void setup() {
		paciente1 = new AID();
		queue = new HashMap<String, Float>();
		queue2 = new HashMap<String, Float>();
		estados = new ArrayList<Float>();

		List = new DynamicJList();
		List.setTitle(this.getLocalName().toString());

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

		if (this.getLocalName().toString().equals("Triagem"))
			addBehaviour(new CheckUp());

		String currentDirFile = System.getProperty("user.dir");

		File file = new File(currentDirFile + "\\" + "resources" + "\\"
				+ this.getLocalName() + ".txt");

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

		if (!this.getLocalName().toString().equals("Triagem"))
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
		File file = new File(currentDirFile + "\\" + "resources" + "\\"
				+ getAID().getLocalName() + ".txt");

		if (file.exists()) {
			file.delete();
		}

		System.out.println("Sala " + getAID().getLocalName() + " fechou!");
	}

	private class CheckUp extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		String timeStamp;

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

					float estado = checkUp(conjuntoSintomas[0]);
					info.setPerformative(ACLMessage.INFORM);
					info.setContent(Float.toString(estado));

					String salanome = conjuntoSintomas[1].substring(1,
							conjuntoSintomas[1].length() - 1);
					String[] salasf = salanome.split(", ");
									
					for (int i = 0; i < salasf.length; i++) {
						writeFile(salasf[i].toString(), msg.getSender()
								.toString(), estado);
					}

					timeStamp = new SimpleDateFormat("HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					
					//criar txt para todos os pacientes para saber se esta ocupado ou nao
					writePacientes(msg.getSender().toString(), "livre");
					
					List.adiciona(msg.getSender().getLocalName().trim() + "   "
							+ estado + "   " + timeStamp);
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

			//System.out.print(currentDirFile + "\\" + "resources" + "\\" + Sala+ ".txt");
			File file = new File(currentDirFile + "\\" + "resources" + "\\"
					+ Sala + ".txt");

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
	
	public static void writePacientes(String Sender, String estado) {
		try {

			String content = Sender + ";" + estado + "\n";
			String currentDirFile = System.getProperty("user.dir");

			//System.out.print(currentDirFile + "\\" + "pac" + "\\" + "pacientes.txt");
			File file = new File(currentDirFile + "\\" + "pac" + "\\" + "pacientes.txt");

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

	public static StringBuffer Readfile(String Sala,
			HashMap<String, Float> queue) {

		StringBuffer stringBuffer = null;

		try {

			String currentDirFile = System.getProperty("user.dir");

			File file = new File(currentDirFile + "\\" + "resources" + "\\"
					+ Sala + ".txt");

			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			stringBuffer = new StringBuffer();
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);

				String[] parts = line.split(";");
				String name = parts[0];
				String estado = parts[1];

				queue.put(name, Float.parseFloat(estado));
				stringBuffer.append("\n");

			}

			fileReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return stringBuffer;

	}

	public static String getKeyFromValue(HashMap<String, Float> hm, Float value) {
		for (String o : hm.keySet()) {
			if (hm.get(o).equals(value)) {
				return o;
			}
		}
		return null;
	}

	public static void removerDoFicheiro(String lineToRemove, String Sala) {

		try {

			String currentDirFile = System.getProperty("user.dir");
			File inFile = new File(currentDirFile + "\\" + "resources" + "\\"
					+ Sala + ".txt");

			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}

			// Construct the new file that will later be renamed to the original
			// filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(
					currentDirFile + "\\" + "resources" + "\\" + Sala + ".txt"));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Read from the original file and write to the new
			// unless content matches data to be removed.
			while ((line = br.readLine()) != null) {

				if (!line.substring(25, 25 + lineToRemove.length()).trim()
						.equals(lineToRemove)) {
					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();

			// Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public static void removerTodos(String lineToRemove) throws IOException {

		String dirName = System.getProperty("user.dir");
		
		File dir = new File(dirName + "\\" + "resources" + "\\");
		File[] files = dir.listFiles();
		for (File file : files) {

			File tempFile = new File(file.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Read from the original file and write to the new
			// unless content matches data to be removed.
			while ((line = br.readLine()) != null) {

				if (!line.substring(25, 25 + lineToRemove.length()).trim()
						.equals(lineToRemove)) {
					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();

			// Delete the original file
			if (!file.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(file))
				System.out.println("Could not rename file");

		}

	}

	public static List<String> editaLinha(List<String> lines, String key, String estado) {
	   
		List<String> newLines = new ArrayList<String>();
        for(String line: lines){
            if(line.contains(key)){
                String [] vals = line.split(";");
                newLines.add(vals[0]+";"+String.valueOf(estado));
            }else{
                newLines.add(line);
            }

        }
        return newLines;
	}
	
	public static List<String> editaLinha2(List<String> lines, String key, String estado) {
		   
		List<String> newLines = new ArrayList<String>();
		
        for(String line: lines){
            if(line.substring(25, 25 + key.length()).trim().contains(key)){
                String [] vals = line.split(";");
                newLines.add(vals[0]+";"+String.valueOf(estado));
            }else{
                newLines.add(line);
            }

        }
        return newLines;
	}
	
	public static void atualizarEstado(String EstadoAtual, String NovoEstado)
			throws IOException {

		String dirName = System.getProperty("user.dir");
		File dir = new File(dirName + "\\" + "resources" + "\\");
		File[] files = dir.listFiles();
		for (File file : files) {
			String content = new String(Files.readAllBytes(file.toPath()),
					StandardCharsets.UTF_8);
			content = content.replaceAll(EstadoAtual, NovoEstado);
			Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
		}
	}

	public String Ocupado(String key) {
		
		String retorna="";
		try {
			
			String currentDirFile = System.getProperty("user.dir");
			File inFile = new File(currentDirFile + "\\" + "pac" + "\\" + "pacientes.txt");

			// Construct the new file that will later be renamed to the original
			// filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(
					currentDirFile + "\\" + "pac" + "\\"  + "pacientes.txt"));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Read from the original file and write to the new
			// unless content matches data to be removed.
			while ((line = br.readLine()) != null) {
				
				String[] coiso = line.split(";");
				
				if (coiso[0].equals(key)) {
					retorna = coiso[1];
				}
			}
			pw.close();
			br.close();


		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return retorna;
	}
	
	private class OfferRequestsServer extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		private int step = 0;
		StringBuffer stringBuffer;
		Float min = 2f;
		String key = "", diferenca="";
		AID pac = null;
		float novo,atual;
		String timeStamp;
		int lines = 0;
		
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
				stringBuffer = Readfile(myAgent.getLocalName().toString(),queue);
				queue2 = queue;
				
				String currentDirFiles = System.getProperty("user.dir");
				File fff = new File(currentDirFiles + "\\" + "pac" + "\\" + "pacientes.txt");
				lines=0;
				
				if(fff.exists() && !fff.isDirectory()) { 
					try {
						
						BufferedReader reader = new BufferedReader(new FileReader(fff));
						while (reader.readLine() != null) lines++;
						reader.close();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
	
				
				// escolhe o valor minimo
				if (stringBuffer.length() != 0) {
							
						min = Collections.min(queue.values());
						key = getKeyFromValue(queue, min);
						if(Ocupado(key).equals("ocupado") && lines > 1) {
							while(Ocupado(key).equals("ocupado") && queue2.size() > 1) {
								queue2.remove(key);
								min = Collections.min(queue2.values());
								key = getKeyFromValue(queue2, min);
							}
						}
						else if(Ocupado(key).equals("ocupado") && lines <= 1){
							break;
						}
						
				} else {
					break;
				}

				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("alocar-pacientes");
				template.addServices(sd);

				try {
					DFAgentDescription[] result = DFService.search(myAgent,
							template);
					for (int i = 0; i < result.length; ++i) {
						if (result[i].getName().toString().equals(key)) {
							pac = result[i].getName();
							
							String currentDirFile = System.getProperty("user.dir");
							File f = new File(currentDirFile + "\\" + "pac" + "\\" + "pacientes.txt");
					        linhas = Files.readAllLines(f.toPath(),Charset.defaultCharset());
					        editaLinha(linhas, key , "ocupado"); // the name and the value you want to modify
					        Files.write(f.toPath(), editaLinha(linhas, key, "ocupado"), Charset.defaultCharset());
							
							advance = true;
						}

					}

				} catch (FIPAException fe) {
					fe.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				marcaconsulta.addReceiver(pac);
				marcaconsulta.setContent("consulta-marcada");
				marcaconsulta.setConversationId("marcar-recurso");
				marcaconsulta.setReplyWith("marcar"
						+ System.currentTimeMillis());

				myAgent.send(marcaconsulta);

				if (advance)
					step = 1;
				break;

			case 1:

				MessageTemplate mt = MessageTemplate
						.MatchPerformative(ACLMessage.INFORM);
				ACLMessage reply = myAgent.receive(mt);
				
				
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.INFORM
							&& reply.getConversationId() == "alocar-recurso") {

						atual = Float.valueOf(reply.getContent().toString());
						
						novo = novoEstado(myAgent.getLocalName().trim(), String.valueOf(atual));
												
						int aval = Float.compare(novo, atual);
						
						if (Float.valueOf(novo).equals(0.0f)) {
							try {
								diferenca = "morreu";
								removerTodos(pac.getName().toString());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						else if(Float.valueOf(novo).equals(1.0f)) {
							try {
								diferenca = "curou";
								removerTodos(pac.getName().toString());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						else if( aval >= 0) {
							diferenca = "melhorou";
							removerDoFicheiro(pac.getName().toString(), myAgent.getLocalName().toString());
						}
						else {
							diferenca = "piorou";
						}
						
						queue.values().remove(Float.valueOf(reply.getContent().toString()));

						try {
							atualizarEstado(reply.getContent().trim(),
									String.valueOf(novo));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						ACLMessage order = new ACLMessage(
								ACLMessage.ACCEPT_PROPOSAL);
						order.addReceiver(pac);
						order.setContent(String.valueOf(novo)+";"+diferenca);
						order.setConversationId("concluir");
						order.setReplyWith("concluir"
								+ System.currentTimeMillis());

						myAgent.send(order);
						
						step = 2;

					}
				}

				else {
					block();
				}

				break;

			case 2:

				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage theend = myAgent.receive(mt);

				if (theend != null
						&& theend.getConversationId() == "terminar-paciente") {
					
					String currentDirFile = System.getProperty("user.dir");
					File f = new File(currentDirFile + "\\" + "pac" + "\\" + "pacientes.txt");
			        try {
						linhas = Files.readAllLines(f.toPath(),Charset.defaultCharset());
						editaLinha2(linhas, pac.getName().toString() , "livre"); 
				        Files.write(f.toPath(), editaLinha(linhas, pac.getName().toString(), "livre"), Charset.defaultCharset());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String resposta = theend.getContent().toString();
					System.out.println("Foi libertada a sala! " + resposta
							+ " --- " + pac.getLocalName() + "   ---  "
							+ myAgent.getLocalName().toString());

					timeStamp = new SimpleDateFormat("HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					List.adiciona(pac.getLocalName().toString() + " -- "
							+ timeStamp + " -- " +  atual  + " -- " +  novo);
					step = 0;
				}

				else {
					block();

				}

				break;
			}
		}
	} // End of inner class OfferRequestsServer

	public float checkUp(String sintomas) {

		String salanome = sintomas.substring(1, sintomas.length() - 1);
		String[] sintoma = salanome.split(", ");
		Random rand = new Random();

		float estado = 1;
		float valor = 0;

		for (int i = 0; i < sintoma.length; i++) {
			switch (sintoma[i]) {

			case "cancro":
				valor = (rand.nextFloat() * (0.5f - 0.0f) + 0.0f) + 0.05f;
				estado -= valor;
				break;
			case "calos":
				valor = (rand.nextFloat() * (0.1f - 0.0f) + 0.0f) + 0.05f;
				estado -= valor;
				break;
			case "baleado":
				valor = (rand.nextFloat() * (0.4f - 0.0f) + 0.0f) + 0.05f;
				estado -= valor;
				break;
			case "gravidez":
				valor = (rand.nextFloat() * (0.2f - 0.0f) + 0.0f) + 0.05f;
				estado -= valor;
				break;
			case "caries":
				valor = (rand.nextFloat() * (0.1f - 0.0f) + 0.0f) + 0.05f;
				estado -= valor;
				break;
			case "febre":
				valor = (rand.nextFloat() * (0.2f - 0.0f) + 0.0f) + 0.05f;
				estado -= valor;
				break;
			default:
				break;
			}

		}

		return estado;

	}
	
	public float novoEstado(String nomeSala, String EstadoAntigo){
		
		float novo;
		Random rand = new Random();
		
		if (nomeSala.equals("Oncologia") || nomeSala.equals("Urgência"))
			novo =
			(rand.nextFloat()*(Float.valueOf(EstadoAntigo)-(-1))+(-1));
		
		else
			novo = (rand.nextFloat()*(Float.valueOf(EstadoAntigo)-(-0.1f))+(-0.1f));

		novo += Float.valueOf(EstadoAntigo);
		
		if(novo > 1)
			novo = 1;
		
		else if(novo < 0)
			novo = 0;
		
		return novo;
	}

}
