package com.sigmap.common.email_trace;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_trace")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailTrace extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @Column(name = "type_email")
    @Enumerated(EnumType.STRING)
    private TypeEmail type_email;

    @Column(name = "objet",columnDefinition = "TEXT")
    @Lob
    private String objet;

    @Column(name = "contenu")
    private String contenu;

    @Column(name = "destinataire")
    private String destinataire;

    @Column(name = "success_send")
    private boolean successSend ;

    @Column(name = "date_send")
    private LocalDateTime date_send;

}
