package com.ouroboros.auth.adapter.out.persistence;

import com.ouroboros.auth.domain.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * Consome (deleta) o token por hash de forma atomica em uma unica query SQL, retornando o numero
   * de linhas afetadas. Em rotacao concorrente do mesmo token, apenas uma transacao recebe 1 (vence
   * a corrida); as demais recebem 0 e devem falhar.
   */
  @Modifying(clearAutomatically = true)
  @Query("delete from RefreshToken r where r.tokenHash = :tokenHash")
  int consumeByTokenHash(@Param("tokenHash") String tokenHash);
}
