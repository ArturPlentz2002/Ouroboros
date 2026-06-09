package com.ouroboros.analytics.adapter.out.persistence;

import com.ouroboros.analytics.domain.UserActivity;
import com.ouroboros.analytics.domain.UserActivityKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface UserActivityRepository extends CassandraRepository<UserActivity, UserActivityKey> {

  /** Lista a atividade da particao do usuario (ordem de clustering: occurred_at desc). */
  List<UserActivity> findByKeyUserId(UUID userId);
}
