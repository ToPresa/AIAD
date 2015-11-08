package agentes;

import FIPA.DateTime;

public class M�dico {
	
	DateTime  HoraEntrada;
	DateTime  HoraSaida;
	boolean ocupado;
	
	public M�dico(DateTime  HoraEntrada, DateTime  HoraSaida, boolean ocupado){
		this.HoraEntrada = HoraEntrada;
		this.HoraSaida = HoraSaida;
		this.ocupado = false;
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
	
	public boolean getOcupado() {
		return ocupado;
	}
	
	public void setOcupado(boolean ocupado) {
		this.ocupado = ocupado;
	}

	
}
