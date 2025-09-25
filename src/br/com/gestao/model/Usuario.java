package br.com.gestao.model;

import br.com.gestao.model.enums.PerfilUsuario;

public class Usuario {
    private String nomeCompleto;
    private String cpf;
    private String email;
    private String cargo;
    private String login;
    private String senha;
    private Equipe equipe;
    private PerfilUsuario perfil;
	
	public void setEmail(String email) { this.email = email; }
	public void setCargo(String cargo) { this.cargo = cargo; }
	public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }


    public Usuario(String nomeCompleto, String cpf, String email, String cargo,
                   String login, String senha, PerfilUsuario perfil) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.email = email;
        this.cargo = cargo;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
    }

    public String getNomeCompleto() { return nomeCompleto; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getCargo() { return cargo; }
    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public String getEquipe() { return equipe.toString(); }
    public PerfilUsuario getPerfil() { return perfil; }

    @Override
    public String toString() {
        return "Usuario{" +
                "nome='" + nomeCompleto + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", cargo='" + cargo + '\'' +
                ", perfil=" + perfil +
                '}';
    }
	
	
}
