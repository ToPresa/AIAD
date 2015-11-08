package agentes;

import FIPA.DateTime;

public class Exames {
	
	String nomePaciente;
	DateTime duracao;
	int resultado; //atribiu um valor dentro de uma escala
	
	public Exames(int Resultado, String NomePaciente, DateTime duracao){
		this.nomePaciente = NomePaciente;
		this.resultado = Resultado;
		this.duracao = duracao;
	}
	
	public String getNomePaciente() {
		return nomePaciente;
	}

	public void setNomePaciente(String nomePaciente) {
		this.nomePaciente = nomePaciente;
	}
	
	public int getResultado() {
		return resultado;
	}

	public void setResultado(int resultado) {
		this.resultado = resultado;
	}
	
	public DateTime getDuracao() {
		return duracao;
	}
	
	public void setDuracao(DateTime  duracao) {
		this.duracao = duracao;
	}
	
}
