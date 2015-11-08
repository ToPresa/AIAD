package agentes;

public class Exames {
	
	String nomePaciente;
	int resultado; //atribiu um valor dentro de uma escala
	
	public Exames(int Resultado, String NomePaciente){
		this.nomePaciente = NomePaciente;
		this.resultado = Resultado;
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
	
}
