package agentes;

import jade.core.AID;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

public class HospitalState {
	
	//List salas = new ArrayList();
	private Sala[] salas;
	Sala sala = new Sala(false, "cancro");
	
	 public HospitalState() {
		// this.salas = new Vector<Sala>();
		 //salas.add(sala);
	}
	 
	 public void setPaciente(Sala paciente) {
			salas[0] = paciente;
	}
}
