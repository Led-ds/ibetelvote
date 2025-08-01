package com.br.ibetelvote.domain.handlers;

import java.util.ArrayList;
import java.util.List;

import com.br.ibetelvote.domain.handlers.exceptions.AcessoNegadoException;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AcessoNegadoHandler {

    @ExceptionHandler(AcessoNegadoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public List<String> errorHandler(AcessoNegadoException e) {
        List<String> errors = new ArrayList<String>();
        errors.add(e.getMessage());
        return errors;
    }


}

