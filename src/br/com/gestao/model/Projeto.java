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
    private Equipe equipe; // associação

    public Projeto(String nome, String descricao, LocalDate dataInicio,
                   LocalDate dataFimPrevista, StatusProjeto status, Usuario gerenteResponsavel) {
        setNome(nome); // usa o setter com validação
        this.descricao = descricao;
        validarDatas(dataInicio, dataFimPrevista);
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.status = status;
        this.gerenteResponsavel = gerenteResponsavel;
    }

    // ==== MÉTODO AUXILIAR DE VALIDAÇÃO ====
    private void validarDatas(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data de término não pode ser anterior à data de início.");
        }
    }

    // ==== GETTERS ====
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFimPrevista() { return dataFimPrevista; }
    public StatusProjeto getStatus() { return status; }
    public Usuario getGerenteResponsavel() { return gerenteResponsavel; }
    public Equipe getEquipe() { return equipe; }

    // ==== SETTERS ====
    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do projeto não pode ser nulo ou vazio.");
        }
        this.nome = nome;
    }

    public void setDescricao(String descricao) { this.descricao = descricao; }

    public void setDataInicio(LocalDate dataInicio) {
        validarDatas(dataInicio, this.dataFimPrevista);
        this.dataInicio = dataInicio;
    }

    public void setDataFimPrevista(LocalDate dataFimPrevista) {
        validarDatas(this.dataInicio, dataFimPrevista);
        this.dataFimPrevista = dataFimPrevista;
    }

    public void setStatus(StatusProjeto status) { this.status = status; }
    public void setGerenteResponsavel(Usuario gerenteResponsavel) { this.gerenteResponsavel = gerenteResponsavel; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

    @Override
    public String toString() {
        return "Projeto{" +
                "nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", dataInicio=" + dataInicio +
                ", dataFimPrevista=" + dataFimPrevista +
                ", status=" + status +
                ", gerente=" + (gerenteResponsavel != null ? gerenteResponsavel.getNomeCompleto() : "N/A") +
                ", equipe=" + (equipe != null ? equipe.getNome() : "N/A") +
                '}';
    }
}
