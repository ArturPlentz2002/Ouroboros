package com.ouroboros.notes.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NoteIT {

  private static final String BASE = "/api/v1/notes";

  @Container static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

  @DynamicPropertySource
  static void mongoProps(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
  }

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;

  private static RequestPostProcessor asUser(String userId) {
    return jwt().jwt(builder -> builder.subject(userId));
  }

  private static String noteJson(String title) {
    return "{\"title\":\"" + title + "\",\"content\":\"corpo\",\"tags\":[\"t1\",\"t2\"]}";
  }

  private String createNote(String userId, String title) throws Exception {
    String body =
        mvc.perform(
                post(BASE)
                    .with(asUser(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(noteJson(title)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.tags.length()").value(2))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body).get("id").asText();
  }

  private void createNote(String userId, String title, String content, String tagsJson)
      throws Exception {
    String json =
        "{\"title\":\"" + title + "\",\"content\":\"" + content + "\",\"tags\":" + tagsJson + "}";
    mvc.perform(
            post(BASE).with(asUser(userId)).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated());
  }

  @Test
  void semTokenRetorna401() throws Exception {
    mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
  }

  @Test
  void criaEListaEscopadoPorUsuario() throws Exception {
    String userA = UUID.randomUUID().toString();
    String userB = UUID.randomUUID().toString();

    createNote(userA, "Nota A");

    mvc.perform(get(BASE).with(asUser(userA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Nota A"));

    mvc.perform(get(BASE).with(asUser(userB)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void notaDeOutroUsuarioRetorna404() throws Exception {
    String owner = UUID.randomUUID().toString();
    String other = UUID.randomUUID().toString();
    String id = createNote(owner, "Privada");

    mvc.perform(get(BASE + "/" + id).with(asUser(other))).andExpect(status().isNotFound());
    mvc.perform(get(BASE + "/" + id).with(asUser(owner))).andExpect(status().isOk());
  }

  @Test
  void atualizaEDeleta() throws Exception {
    String userId = UUID.randomUUID().toString();
    String id = createNote(userId, "Original");

    mvc.perform(
            put(BASE + "/" + id)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson("Atualizada")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Atualizada"));

    mvc.perform(delete(BASE + "/" + id).with(asUser(userId))).andExpect(status().isNoContent());
    mvc.perform(get(BASE + "/" + id).with(asUser(userId))).andExpect(status().isNotFound());
  }

  @Test
  void rejeitaTituloEmBrancoCom400() throws Exception {
    String userId = UUID.randomUUID().toString();

    mvc.perform(
            post(BASE)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"content\":\"x\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void buscaPorTagFiltra() throws Exception {
    String userId = UUID.randomUUID().toString();
    createNote(userId, "Trabalho", "reuniao", "[\"work\"]");
    createNote(userId, "Pessoal", "academia", "[\"home\"]");

    mvc.perform(get(BASE).param("tag", "work").with(asUser(userId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Trabalho"));
  }

  @Test
  void buscaPorTextoEmTituloOuConteudoCaseInsensitive() throws Exception {
    String userId = UUID.randomUUID().toString();
    createNote(userId, "Compras", "Comprar LEITE e pao", "[]");
    createNote(userId, "Treino", "corrida no parque", "[]");

    mvc.perform(get(BASE).param("q", "leite").with(asUser(userId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Compras"));
  }

  @Test
  void buscaEscopadaPorUsuario() throws Exception {
    String owner = UUID.randomUUID().toString();
    String other = UUID.randomUUID().toString();
    createNote(owner, "Privada", "segredo", "[\"x\"]");

    mvc.perform(get(BASE).param("tag", "x").with(asUser(other)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
