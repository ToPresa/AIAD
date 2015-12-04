package proj;
import java.util.ArrayList;

public class Sala {
	
	String nome;
	boolean ocupada;
	ArrayList<String> Sintomas;
	
	public Sala(String nome){
		this.nome = nome;
		this.ocupada = false;
		this.Sintomas = new ArrayList<String>();
	}
	
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public boolean getOcupada() {
		return ocupada;
	}

	public void setOcupada(boolean ocupada) {
		this.ocupada = ocupada;
	}
	
	public ArrayList<String> getSintomas() {
		return Sintomas;
	}	
	
	public void setSintomas(String NomeSala) {
		if(NomeSala.equals("Oncologia")) {
			Sintomas.add("o");
		}
		else if(NomeSala.equals("Pediatria")) {
			Sintomas.add("p");
		}
		else if(NomeSala.equals("Urgência")) {
			Sintomas.add("u");
		}
		else if(NomeSala.equals("Ortopedia")) {
			Sintomas.add("or");
		}
		else if(NomeSala.equals("Genecologia")) {
			Sintomas.add("g");
		}
		else if(NomeSala.equals("Urologia")) {
			Sintomas.add("ur");
		}
		else if(NomeSala.equals("Medicina Dentária")) {
			Sintomas.add("m");
		}
		else if(NomeSala.equals("Cirurgia")) {
			Sintomas.add("c");
		}
		else if(NomeSala.equals("Dermatologia")) {
			Sintomas.add("d");
		}
		else{System.out.println("Não existe essa sala!");}
	}

}
