package com.springboot.picpay.simplified.controller;

import com.springboot.picpay.simplified.dto.request.UsuarioRequestDTO;
import com.springboot.picpay.simplified.dto.response.UsuarioResponseDTO;
import com.springboot.picpay.simplified.exception.InvalidDocumentException;
import com.springboot.picpay.simplified.exception.InvalidUserRequestDataException;
import com.springboot.picpay.simplified.exception.UsuarioNotFoundException;
import com.springboot.picpay.simplified.model.TipoUsuario;
import com.springboot.picpay.simplified.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve criar usuario com sucesso")
    void deveCriarUsuarioComSucesso() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678910", TipoUsuario.COMUM);
        UsuarioResponseDTO response = new UsuarioResponseDTO(
                1L, "Gabriel", "12345678910", "Gabriel@email.com", BigDecimal.ZERO, TipoUsuario.COMUM);

        when(usuarioService.criarUsuario(any())).thenReturn(response);

        mockMvc.perform(post("/api/usuarios/criar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Gabriel"))
                .andExpect(jsonPath("$.documento").value("12345678910"));

        verify(usuarioService).criarUsuario(dto);
    }

    @Test
    @DisplayName("Deve retornar usuário com sucesso ao buscar por id")
    void deveBuscarUsuarioPorIdComSucesso() throws Exception {
        UsuarioResponseDTO response = new UsuarioResponseDTO(
                1L, "Gabriel", "12345678910", "Gabriel@email.com", BigDecimal.ZERO, TipoUsuario.COMUM);

        when(usuarioService.findUsuarioResponseById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/usuarios/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Gabriel"));

        verify(usuarioService).findUsuarioResponseById(1L);
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso por id")
    void deveBuscarUsuarioPorIdEDeletarComSucesso() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(usuarioService).desativarUsuario(1L);
    }

    @Test
    @DisplayName("Deve retornar bad request quando valores preenchidos forem inválidos")
    void deveRetornarBadRequestQuandoDadosForemInvalidos() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "1234567891K", TipoUsuario.COMUM);

        mockMvc.perform(post("/api/usuarios/criar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    @DisplayName("Deve retornar conflict quando dados já estiverem cadastrados")
    void deveRetornarConflictQuandoDadosJaCadastrados() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678910", TipoUsuario.COMUM);

        when(usuarioService.criarUsuario(dto)).thenThrow(new InvalidUserRequestDataException("Já existe um usuário com esse email"));

        mockMvc.perform(post("/api/usuarios/criar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());

        verify(usuarioService).criarUsuario(dto);
    }

    @Test
    @DisplayName("Deve retornar exception quando documento inválido")
    void deveRetornarExceptionQuandoDocumentoNaoBaterComTipoUsuario() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Gabriel", "Gabriel@email.com", "12345678910", TipoUsuario.COMUM);

        when(usuarioService.criarUsuario(dto)).thenThrow(new InvalidDocumentException("CPF deve conter 11 dígitos."));

        mockMvc.perform(post("/api/usuarios/criar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF deve conter 11 dígitos."));

        verify(usuarioService).criarUsuario(dto);
    }

    @Test
    @DisplayName("Deve retornar exception quando usuário não encontrado")
    void deveRetornarExceptionQuandoUsuarioNaoEncontrado() throws Exception {
        when(usuarioService.findUsuarioResponseById(999L)).thenThrow(new UsuarioNotFoundException("Usuário não foi encontrado com o id: 999"));

        mockMvc.perform(get("/api/usuarios/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não foi encontrado com o id: 999"));

        verify(usuarioService).findUsuarioResponseById(999L);
    }

}
