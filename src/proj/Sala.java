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
}
