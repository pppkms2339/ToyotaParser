package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.SubgroupCatalog;

@Repository
public interface SubgroupCatalogRepository extends JpaRepository<SubgroupCatalog, Long> {
}
