package com.br.ibetelvote.application.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPhotoResponse {
    private String fileName;
    private String fotoBase64;
    private String message;
}