package com.example.miapp.repository;

import com.example.miapp.entity.Notification;
import com.example.miapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUser(User user, Pageable pageable);
    
    Page<Notification> findByUserAndStatus(User user, Notification.NotificationStatus status, Pageable pageable);
    
    long countByUserAndStatus(User user, Notification.NotificationStatus status);
}