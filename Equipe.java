package br.com.gestao.model;

import java.util.ArrayList;
import java.util.List;

public class Equipe {
    private String nome;
    private String descricao;
    private List<Usuario> membros = new ArrayList<>();

    public Equipe(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public void adicionarMembro(Usuario usuario) {
        membros.add(usuario);
    }

    public void removerMembro(String cpf) {
        membros.removeIf(u -> u.getCpf().equals(cpf));
    }

    @Override
    public String toString() {
        return "Equipe{" +
                "nome='" + nome + '\'' +
                ", membros=" + membros.size() +
                '}';
    }
	
	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }
	public void setDescricao(String descricao) { this.descricao = descricao; }
}
