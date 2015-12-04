package proj;

public class Pessoa {

	String nome;
	float estado;
	
	public Pessoa(String nome){
		this.nome = nome;
		this.estado = 1;
	}
	
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public float getEstado() {
		return estado;
	}

	public void setEstado(float estado) {
		this.estado = estado;
	}
}
