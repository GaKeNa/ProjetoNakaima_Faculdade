package br.com.gestao.model;

import br.com.gestao.model.enums.StatusProjeto;

import java.time.LocalDate;

public class Projeto {
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFimPrevista;
    private StatusProjeto status;
    private Usuario gerenteResponsavel;

    public Projeto(String nome, String descricao, LocalDate dataInicio,
                   LocalDate dataFimPrevista, StatusProjeto status, Usuario gerenteResponsavel) {
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.status = status;
        this.gerenteResponsavel = gerenteResponsavel;
    }

    public String getNome() { return nome; }
    public StatusProjeto getStatus() { return status; }
    public Usuario getGerenteResponsavel() { return gerenteResponsavel; }

    @Override
    public String toString() {
        return "Projeto{" +
                "nome='" + nome + '\'' +
                ", status=" + status +
                ", gerente=" + (gerenteResponsavel != null ? gerenteResponsavel.getNomeCompleto() : "N/A") +
                '}';
    }
	
	public void setStatus(StatusProjeto status) { this.status = status; }
}
