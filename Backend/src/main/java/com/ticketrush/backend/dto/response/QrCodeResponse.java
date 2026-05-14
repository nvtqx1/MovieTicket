package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response chứa dữ liệu mã QR.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCodeResponse {

    private String base64String;

    private String dataUri;

    private String encodedData;

    private String imageSize;

    private String imageFormat;

    private String status;

    private String message;

    private Long fileSizeBytes;

    private String createdAt;

    private Long entityId;

    private String entityType;
}

