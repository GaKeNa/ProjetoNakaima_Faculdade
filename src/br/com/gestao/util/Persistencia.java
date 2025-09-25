package br.com.gestao.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import br.com.gestao.util.LocalDateAdapter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public class Persistencia {
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .setPrettyPrinting()
        .create();

    public static <T> void salvar(String caminho, T dados) {
        try (FileWriter writer = new FileWriter(caminho)) {
            gson.toJson(dados, writer);
        } catch (IOException e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }

    public static <T> T carregar(String caminho, Type tipo) {
        try (FileReader reader = new FileReader(caminho)) {
            return gson.fromJson(reader, tipo);
        } catch (IOException e) {
            System.out.println("Arquivo não encontrado: " + caminho);
            return null;
        }
    }
}
