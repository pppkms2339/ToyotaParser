package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.Component;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {

}
