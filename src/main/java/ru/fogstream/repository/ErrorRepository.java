package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.ToyotaError;

import java.util.List;

@Repository
public interface ErrorRepository extends JpaRepository<ToyotaError, Long> {

    @Query(value = "SELECT * FROM errors WHERE id > ((SELECT count(*) FROM errors) - 40) ORDER BY id", nativeQuery = true)
    List<ToyotaError> getLastErrors();

}