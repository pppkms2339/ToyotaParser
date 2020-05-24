package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.GroupCatalog;

@Repository
public interface GroupCatalogRepository extends JpaRepository<GroupCatalog, Long> {
}
