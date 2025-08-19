package com.chat_app.security;


import com.chat_app.constant.Constants;
import com.chat_app.service.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

    private final JwtService jwtService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var response = jwtService.verifyToken(token);
            if (!response) throw new BadJwtException("Token invalid");
        } catch (Exception e) {
            throw new BadJwtException(e.getMessage());
        }
        if (Objects.isNull(nimbusJwtDecoder)) {
            byte[] decodedKey = Base64.getDecoder().decode(signerKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, Constants.JWT_SIGNATURE_ALGORITHM);
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }
        return nimbusJwtDecoder.decode(token);
    }
}