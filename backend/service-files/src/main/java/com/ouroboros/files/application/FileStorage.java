package com.ouroboros.files.application;

/** Porta de armazenamento de blobs (implementada por um adapter de objeto, ex.: S3). */
public interface FileStorage {

  /** Grava o conteudo sob a chave informada. */
  void put(String key, byte[] content, String contentType);

  /** Le o conteudo armazenado sob a chave. */
  byte[] get(String key);

  /** Remove o objeto sob a chave (idempotente). */
  void delete(String key);
}
