package com.mycompany.myapp.web.rest.vm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * View Model for incoming chat prompts.
 */
public class ChatRequestVM {

    @NotBlank
    @Size(max = 10000)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
