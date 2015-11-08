package agentes;
import java.util.Vector;

public class Sala {
	//sala->recurso
	
	boolean ocupada;
	String TipoExame;
	Vector<Enfermeiro> enfermeiros;
	
	public Sala(boolean ocupada, String TipoExame){
		this.ocupada = ocupada;
		this.TipoExame = TipoExame;
		this.enfermeiros = new Vector<Enfermeiro>();
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
}
