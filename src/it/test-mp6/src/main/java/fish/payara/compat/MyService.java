package fish.payara.compat;

import fish.payara.compat.MyEntity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

public class MyService {

    @Inject
    private EntityManager em;

    @Transactional
    public void save(MyEntity entity) {
        em.persist(entity);
    }
}
