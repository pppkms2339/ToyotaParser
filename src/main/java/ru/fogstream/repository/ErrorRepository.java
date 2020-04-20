package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.ToyotaError;

@Repository
public interface ErrorRepository extends JpaRepository<ToyotaError, Long> {

}
