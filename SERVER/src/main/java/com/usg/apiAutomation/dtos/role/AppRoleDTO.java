package com.usg.apiAutomation.dtos.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data Transfer Object for application roles")
public class AppRoleDTO {

    @Schema(description = "Unique identifier of the role", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID roleId;

    @NotBlank(message = "Role name cannot be blank")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Schema(description = "Name of the role", example = "ADMIN")
    private String roleName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Description of the role", example = "Administrator role with all privileges")
    private String description;
}