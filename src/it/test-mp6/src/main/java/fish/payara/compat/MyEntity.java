package fish.payara.compat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MyEntity {

    @Id
    private Long id;

    private String name;
}

