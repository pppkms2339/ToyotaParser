package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.Unit;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

}
