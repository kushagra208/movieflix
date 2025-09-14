package com.kushagra.movieflix.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomResponse {
    private String statusCode;
    private String status;
    private String message;
    private Object result;
}
