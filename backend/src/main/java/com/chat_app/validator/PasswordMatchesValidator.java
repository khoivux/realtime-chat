package com.chat_app.validator;

import com.chat_app.dto.request.RegisterRequest;
import com.chat_app.validator.annotation.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequest> {
    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        boolean isValid = request.getPassword() != null && request.getPassword().equals(request.getConfirmPassword());
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("PASSWORD_NOT_MATCHED")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return isValid;
    }
}