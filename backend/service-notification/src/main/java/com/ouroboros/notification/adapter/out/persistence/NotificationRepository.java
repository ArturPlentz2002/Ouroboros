package com.ouroboros.notification.adapter.out.persistence;

import com.ouroboros.notification.domain.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
