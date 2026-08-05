package com.labortrack.security.Model.Dto.Response;

public record AuthLoginResponseDto (String email,
                                    String message,
                                    String jwt,
                                    Boolean status) {}
