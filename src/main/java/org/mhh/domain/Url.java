package org.mhh.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "url")
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 2048)
    private String originalUrl;
    @Column(name = "expiration_date")
    private ZonedDateTime expirationDate;    private int clickCount;
    @Column(unique = true)
    private String shortCode;
}
