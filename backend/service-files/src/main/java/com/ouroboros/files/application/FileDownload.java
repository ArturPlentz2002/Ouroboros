package com.ouroboros.files.application;

import com.ouroboros.files.domain.FileMetadata;

/** Conteudo de um arquivo baixado mais seus metadados. */
public record FileDownload(FileMetadata metadata, byte[] content) {}
