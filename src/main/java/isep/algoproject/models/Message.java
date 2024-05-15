package isep.algoproject.models;

import isep.algoproject.models.enums.MessageCategory;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

public class Message implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String content;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageCategory category;
    @Column(nullable = false)
    private Instant createdAt;
}
