package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.Equipment;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

}
