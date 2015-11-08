package agentes;

import java.util.Vector;

import FIPA.DateTime;

public class Paciente{
	
	int prioridade; //prioridade para ser atendido
	String name; //nome do paciente
	String TipoDoen�a; // /sintoma/ para ser levado ao "departamento" correto, saber o exame a fazer
	DateTime  HoraChegada; //hora que chegou ao hospital
	Vector <Exames> exames;
		
	public Paciente(String name, int prioridade, String TipoDoen�a,DateTime HoraChegada){
		this.name = name;
		this.prioridade = prioridade;
		this.TipoDoen�a = TipoDoen�a;
		this.HoraChegada = HoraChegada;
		this.exames = new Vector<Exames>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(int prioridade) {
		this.prioridade = prioridade;
	}
	
	public String getTipoDoen�a() {
		return TipoDoen�a;
	}

	public void setTipoDoen�a(String TipoDoen�a) {
		this.TipoDoen�a = TipoDoen�a;
	}
	
	public DateTime getHoraChegada() {
		return HoraChegada;
	}

	public void setHoraChegada(DateTime HoraChegada) {
		this.HoraChegada = HoraChegada;
	}
}
