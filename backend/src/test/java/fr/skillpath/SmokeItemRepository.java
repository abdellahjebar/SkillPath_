package fr.skillpath;

import org.springframework.data.repository.CrudRepository;

/** Dépôt de test : un dépôt par agrégat, comme dans les modules. */
public interface SmokeItemRepository extends CrudRepository<SmokeItem, Long> {
}
