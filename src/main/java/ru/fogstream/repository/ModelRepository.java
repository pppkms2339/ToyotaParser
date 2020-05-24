package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.CarModel;

import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<CarModel, Long> {

    Optional<CarModel> findByModelName(String modelName);

}
