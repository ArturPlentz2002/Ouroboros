package com.ouroboros.user.adapter.in.web;

import com.ouroboros.shared.web.AbstractApiExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Reaproveita o tratamento de validacao/erros comum do modulo shared. */
@RestControllerAdvice
public class ApiExceptionHandler extends AbstractApiExceptionHandler {}
