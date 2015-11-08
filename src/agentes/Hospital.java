package agentes;

import java.util.Vector;

public class Hospital {
	
	Vector<Paciente> pacientes;
	Vector<Médico> medicos;
	Vector<Sala> salas;
	Vector<Enfermeiro> enfermeiros;
	
	public Hospital(){
		this.pacientes = new Vector<Paciente>();
		this.medicos = new Vector<Médico>();
		this.salas = new Vector<Sala>();
		this.enfermeiros = new Vector<Enfermeiro>();
	}
	
	public Vector<Paciente> getPacientes() {
		return pacientes;
	}
	public Vector<Médico> getMedicos() {
		return medicos;
	}
	public Vector<Sala> getSalas() {
		return salas;
	}
	public Vector<Enfermeiro> getEnfermeiros() {
		return enfermeiros;
	}
}
