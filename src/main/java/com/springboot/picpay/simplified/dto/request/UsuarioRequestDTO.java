package com.springboot.picpay.simplified.dto.request;

import com.springboot.picpay.simplified.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record UsuarioRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "O documento é obrigatório")
        @Pattern(
                regexp = "^[0-9]+$",
                message = "O documento só deve conter números")
        String documento,

        @NotNull(message = "Tipo de usuário é obrigatório")
        TipoUsuario tipo
) {
}
