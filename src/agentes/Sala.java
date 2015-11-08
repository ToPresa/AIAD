package agentes;
import java.util.Vector;

public class Sala {
	//sala->recurso
	
	boolean ocupada;
	String TipoExame; //tipo de exame esta sala faz (recurso que tem)
	Vector<Enfermeiro> enfermeiros;
	Vector<Exames> exames; //exames para fazer nesta sala
	
	public Sala(boolean ocupada, String TipoExame){
		this.ocupada = ocupada;
		this.TipoExame = TipoExame;
		this.enfermeiros = new Vector<Enfermeiro>();
		this.exames = new Vector<Exames>();
	}
	
	public String getTipoExame() {
		return TipoExame;
	}

	public void setName(String TipoExame) {
		this.TipoExame = TipoExame;
	}
	
	public boolean getOcupada() {
		return ocupada;
	}

	public void setOcupada(boolean ocupada) {
		this.ocupada = ocupada;
	}
	
	public Vector<Enfermeiro> getEnfermeiros() {
		return enfermeiros;
	}	
	
	public Vector<Exames> getExames() {
		return exames;
	}	
}
