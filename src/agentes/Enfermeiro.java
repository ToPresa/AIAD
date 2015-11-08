package agentes;

import FIPA.DateTime;

public class Enfermeiro {
	
	boolean disponivel;
	DateTime  HoraEntrada;
	DateTime  HoraSaida;
	
	public Enfermeiro(boolean disponivel, DateTime HoraEntrada ,DateTime HoraSaida){
		this.disponivel = disponivel;
		this.HoraEntrada = HoraEntrada;
		this.HoraSaida = HoraSaida;
	}
	
	public boolean getDisponivel() {
		return disponivel;
	}

	public void setDisponivel(boolean disponivel) {
		this.disponivel = disponivel;
	}
	
	public DateTime getHoraEntrada() {
		return HoraEntrada;
	}
	
	public void setHoraEntrada(DateTime  HoraEntrada) {
		this.HoraEntrada = HoraEntrada;
	}
	
	public DateTime  getHoraSaida() {
		return HoraSaida;
	}
	
	public void setHoraSaida(DateTime  HoraSaida) {
		this.HoraSaida = HoraSaida;
	}
}
