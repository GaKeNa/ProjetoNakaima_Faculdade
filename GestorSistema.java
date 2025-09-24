package br.com.gestao.manager;

import br.com.gestao.model.*;
import br.com.gestao.model.enums.PerfilUsuario;
import br.com.gestao.model.enums.StatusProjeto;
import br.com.gestao.model.enums.TipoPersistencia;
import br.com.gestao.util.Persistencia;
import br.com.gestao.util.PersistenciaCSV;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.io.File;

public class GestorSistema {
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Projeto> projetos = new ArrayList<>();
    private List<Equipe> equipes = new ArrayList<>();

    private TipoPersistencia tipoPersistencia = TipoPersistencia.JSON; // padrão

    private static final String USUARIOS_ARQ = "usuarios.json";
    private static final String PROJETOS_ARQ = "projetos.json";
    private static final String EQUIPES_ARQ = "equipes.json";

	private static final String USUARIOS_CSV = "usuarios.csv";
    private static final String PROJETOS_CSV = "projetos.csv";
    private static final String EQUIPES_CSV = "equipes.csv";

    // ==== Configuração de persistência ====
    public void setTipoPersistencia(TipoPersistencia tipo) {
        this.tipoPersistencia = tipo;
    }

    public TipoPersistencia getTipoPersistencia() {
        return tipoPersistencia;
    }

    // ==== Salvar tudo ====
    public void salvarTudo() {
        if (tipoPersistencia == TipoPersistencia.JSON) {
            salvarJSON();
			salvarBackupJSON();
        } else {
            salvarCSV();
			salvarBackupCSV();
        }
    }

    // ==== Carregar tudo ====
    public void carregarTudo() {
        if (tipoPersistencia == TipoPersistencia.JSON) {
            carregarJSON();
        } else {
            carregarCSV();
        }
    }

	public List<String> listarBackups(String tipoArquivo) {
		File pasta = new File("."); // pasta atual
		String[] arquivos = pasta.list((dir, name) -> name.startsWith("backup_") && name.endsWith(tipoArquivo));
		if (arquivos != null) {
			Arrays.sort(arquivos); // opcional: ordenar por nome (timestamp)
			return Arrays.asList(arquivos);
		}
		return new ArrayList<>();
	}

	// ==== Backup JSON ====
	private void salvarBackupJSON() {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		Persistencia.salvar("backup_usuarios_" + timestamp + ".json", usuarios);
		Persistencia.salvar("backup_projetos_" + timestamp + ".json", projetos);
		Persistencia.salvar("backup_equipes_" + timestamp + ".json", equipes);
		System.out.println("Backup JSON criado: " + timestamp);
	}

	// ==== Backup CSV ====
	private void salvarBackupCSV() {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		PersistenciaCSV.salvar("backup_usuarios_" + timestamp + ".csv", gerarDadosUsuariosCSV());
		PersistenciaCSV.salvar("backup_projetos_" + timestamp + ".csv", gerarDadosProjetosCSV());
		PersistenciaCSV.salvar("backup_equipes_" + timestamp + ".csv", gerarDadosEquipesCSV());
		System.out.println("Backup CSV criado: " + timestamp);
	}

	// ==== Restaurar BackUp ====
	
	public void restaurarBackupEscolhido() {
		String extensao = tipoPersistencia == TipoPersistencia.JSON ? ".json" : ".csv";

		List<String> backupsUsuarios = listarBackups("usuarios" + extensao);
		List<String> backupsProjetos = listarBackups("projetos" + extensao);
		List<String> backupsEquipes = listarBackups("equipes" + extensao);

		if (backupsUsuarios.isEmpty() || backupsProjetos.isEmpty() || backupsEquipes.isEmpty()) {
			System.out.println("Nenhum backup encontrado!");
			return;
		}

		System.out.println("\nBackups disponíveis de usuários:");
		for (int i = 0; i < backupsUsuarios.size(); i++) {
			System.out.println((i + 1) + " - " + backupsUsuarios.get(i));
		}

		Scanner sc = new Scanner(System.in);
		System.out.print("Escolha o backup a restaurar (usuários/projetos/equipes com mesmo índice): ");
		int escolha = sc.nextInt() - 1;

		if (escolha >= 0 && escolha < backupsUsuarios.size()) {
			restaurarBackup(backupsUsuarios.get(escolha),
							backupsProjetos.get(escolha),
							backupsEquipes.get(escolha));
		} else {
			System.out.println("Índice inválido!");
		}
	}


    // ==== Persistência JSON ====
    private void salvarJSON() {
        Persistencia.salvar(USUARIOS_ARQ, usuarios);
        Persistencia.salvar(PROJETOS_ARQ, projetos);
        Persistencia.salvar(EQUIPES_ARQ, equipes);
        System.out.println("Dados salvos em JSON!");
    }

    private void carregarJSON() {
        Type usuariosType = new TypeToken<ArrayList<Usuario>>() {}.getType();
        Type projetosType = new TypeToken<ArrayList<Projeto>>() {}.getType();
        Type equipesType = new TypeToken<ArrayList<Equipe>>() {}.getType();

        List<Usuario> u = Persistencia.carregar(USUARIOS_ARQ, usuariosType);
        List<Projeto> p = Persistencia.carregar(PROJETOS_ARQ, projetosType);
        List<Equipe> e = Persistencia.carregar(EQUIPES_ARQ, equipesType);

        if (u != null) usuarios = u;
        if (p != null) projetos = p;
        if (e != null) equipes = e;

        System.out.println("Dados carregados de JSON!");
    }

    // ==== Persistência CSV ====
    private void salvarCSV() {
        // Usuários
        List<String[]> dadosUsuarios = new ArrayList<>();
        for (Usuario u : usuarios) {
            dadosUsuarios.add(new String[]{
                    u.getNomeCompleto(),
                    u.getCpf(),
                    u.getEmail(),
                    u.getPerfil().name()
            });
        }
        PersistenciaCSV.salvar(USUARIOS_CSV, dadosUsuarios);

        // Projetos
        List<String[]> dadosProjetos = new ArrayList<>();
        for (Projeto p : projetos) {
            dadosProjetos.add(new String[]{
                    p.getNome(),
                    p.getStatus().name(),
                    p.getGerenteResponsavel() != null ? p.getGerenteResponsavel().getCpf() : "",
                    p.getEquipe() != null ? p.getEquipe().getNome() : ""
            });
        }
        PersistenciaCSV.salvar(PROJETOS_CSV, dadosProjetos);

        // Equipes
        List<String[]> dadosEquipes = new ArrayList<>();
        for (Equipe e : equipes) {
            String membros = String.join(",", e.getMembros().stream().map(Usuario::getCpf).toList());
            dadosEquipes.add(new String[]{e.getNome(), e.toString(), membros});
        }
        PersistenciaCSV.salvar(EQUIPES_CSV, dadosEquipes);

        System.out.println("Dados salvos em CSV!");
    }

    private void carregarCSV() {
        // Usuários
        List<String[]> usuariosCSV = PersistenciaCSV.carregar(USUARIOS_CSV);
        for (String[] u : usuariosCSV) {
            usuarios.add(new Usuario(u[0], u[1], u[2], PerfilUsuario.valueOf(u[3])));
        }

        // Equipes
        List<String[]> equipesCSV = PersistenciaCSV.carregar(EQUIPES_CSV);
        for (String[] e : equipesCSV) {
            Equipe equipe = new Equipe(e[0], e[1]);
            if (e.length > 2 && !e[2].isEmpty()) {
                String[] cpfs = e[2].split(",");
                for (String cpf : cpfs) {
                    usuarios.stream()
                            .filter(u -> u.getCpf().equals(cpf))
                            .findFirst()
                            .ifPresent(equipe::adicionarMembro);
                }
            }
            equipes.add(equipe);
        }

        // Projetos
        List<String[]> projetosCSV = PersistenciaCSV.carregar(PROJETOS_CSV);
        for (String[] p : projetosCSV) {
            Usuario gerente = usuarios.stream()
                    .filter(u -> u.getCpf().equals(p[2]))
                    .findFirst().orElse(null);
            Equipe equipe = equipes.stream()
                    .filter(eq -> eq.getNome().equalsIgnoreCase(p[3]))
                    .findFirst().orElse(null);

            Projeto projeto = new Projeto(
                    p[0], "Carregado do CSV",
                    LocalDate.now(), LocalDate.now().plusDays(30),
                    StatusProjeto.valueOf(p[1]), gerente
            );
            projeto.setEquipe(equipe);
            projetos.add(projeto);
        }

        System.out.println("Dados carregados de CSV!");
    }

	// ==== USUÁRIOS ====
	public void cadastrarUsuario(Usuario usuario) {
		usuarios.add(usuario);
		salvarTudo(); // salva automático
		System.out.println("Usuário cadastrado com sucesso!");
	}

	public void atualizarUsuario(String cpf, String novoNome, String novoEmail) {
		for (Usuario u : usuarios) {
			if (u.getCpf().equals(cpf)) {
				u.setNomeCompleto(novoNome);
				u.setEmail(novoEmail);
				salvarTudo(); // salva automático
				System.out.println("Usuário atualizado!");
				return;
			}
		}
		System.out.println("Usuário não encontrado!");
	}

	public void removerUsuario(String cpf) {
		boolean removido = usuarios.removeIf(u -> u.getCpf().equals(cpf));
		if (removido) {
			salvarTudo(); // salva automático
			System.out.println("Usuário removido!");
		} else {
			System.out.println("Usuário não encontrado!");
		}
	}

	public void listarUsuarios() {
		if (usuarios.isEmpty()) {
			System.out.println("Nenhum usuário cadastrado.");
		} else {
			usuarios.forEach(System.out::println);
		}
	}


    // ==== PROJETOS ====
	public void cadastrarProjeto(Projeto projeto) {
		projetos.add(projeto);
		salvarTudo();
		System.out.println("Projeto cadastrado!");
	}

	public void atualizarProjeto(String nomeProjeto, String novoNome) {
		for (Projeto p : projetos) {
			if (p.getNome().equalsIgnoreCase(nomeProjeto)) {
				p.setNome(novoNome);
				salvarTudo();
				System.out.println("Projeto atualizado!");
				return;
			}
		}
		System.out.println("Projeto não encontrado!");
	}

	public void removerProjeto(String nomeProjeto) {
		boolean removido = projetos.removeIf(p -> p.getNome().equalsIgnoreCase(nomeProjeto));
		if (removido) {
			salvarTudo();
			System.out.println("Projeto removido!");
		} else {
			System.out.println("Projeto não encontrado!");
		}
	}


    // ==== EQUIPES ====
    public void cadastrarEquipe(Equipe equipe) {
		equipes.add(equipe);
		salvarTudo();
		System.out.println("Equipe cadastrada!");
	}

	public void atualizarEquipe(String nomeEquipe, String novoNome) {
		for (Equipe e : equipes) {
			if (e.getNome().equalsIgnoreCase(nomeEquipe)) {
				e.setNome(novoNome);
				salvarTudo();
				System.out.println("Equipe atualizada!");
				return;
			}
		}
		System.out.println("Equipe não encontrada!");
	}

	public void removerEquipe(String nomeEquipe) {
		boolean removido = equipes.removeIf(e -> e.getNome().equalsIgnoreCase(nomeEquipe));
		if (removido) {
			salvarTudo();
			System.out.println("Equipe removida!");
		} else {
			System.out.println("Equipe não encontrada!");
		}
	}

	
	// ==== ASSOCIAÇÕES ====

	// Adicionar usuário em equipe
	public void adicionarUsuarioEmEquipe(String cpf, String nomeEquipe) {
		Usuario usuario = usuarios.stream()
				.filter(u -> u.getCpf().equals(cpf))
				.findFirst().orElse(null);

		Equipe equipe = equipes.stream()
				.filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
				.findFirst().orElse(null);

		if (usuario != null && equipe != null) {
			equipe.adicionarMembro(usuario);
			salvarTudo(); // salva automático
			System.out.println("Usuário adicionado à equipe!");
		} else {
			System.out.println("Usuário ou equipe não encontrados.");
		}
	}

	public void associarEquipeAProjeto(String nomeEquipe, String nomeProjeto) {
		Equipe equipe = equipes.stream()
				.filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
				.findFirst().orElse(null);

		Projeto projeto = projetos.stream()
				.filter(p -> p.getNome().equalsIgnoreCase(nomeProjeto))
				.findFirst().orElse(null);

		if (equipe != null && projeto != null) {
			projeto.setEquipe(equipe);
			salvarTudo(); // salva automático
			System.out.println("Equipe atribuída ao projeto!");
		} else {
			System.out.println("Equipe ou projeto não encontrados.");
		}
	}

	private List<String[]> gerarDadosUsuariosCSV() {
		List<String[]> dados = new ArrayList<>();
		for (Usuario u : usuarios) {
			dados.add(new String[]{u.getNomeCompleto(), u.getCpf(), u.getEmail(), u.getPerfil().name()});
		}
		return dados;
	}

	private List<String[]> gerarDadosProjetosCSV() {
		List<String[]> dados = new ArrayList<>();
		for (Projeto p : projetos) {
			dados.add(new String[]{
					p.getNome(),
					p.getStatus().name(),
					p.getGerenteResponsavel() != null ? p.getGerenteResponsavel().getCpf() : "",
					p.getEquipe() != null ? p.getEquipe().getNome() : ""
			});
		}
		return dados;
	}

	private List<String[]> gerarDadosEquipesCSV() {
		List<String[]> dados = new ArrayList<>();
		for (Equipe e : equipes) {
			String membros = String.join(",", e.getMembros().stream().map(Usuario::getCpf).toList());
			dados.add(new String[]{e.getNome(), e.toString(), membros});
		}
		return dados;
	}


    // ==== GETTERS ====
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Projeto> getProjetos() { return projetos; }
    public List<Equipe> getEquipes() { return equipes; }
}
