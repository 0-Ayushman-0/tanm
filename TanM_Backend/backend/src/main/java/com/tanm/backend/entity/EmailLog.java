package com.tanm.backend.entity;

import com.tanm.backend.common.BaseEntity;
import com.tanm.backend.enums.EmailTemplate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs", indexes = {
        @Index(name = "idx_email_log_recipient", columnList = "recipient"),
        @Index(name = "idx_email_log_template", columnList = "template")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String recipient;

    @Column(nullable = false, length = 200)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailTemplate template;

    @Column(nullable = false, length = 20)
    private String status; // SENT, FAILED

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;
}
