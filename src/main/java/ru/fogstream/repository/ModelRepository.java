package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.CarModel;

@Repository
public interface ModelRepository extends JpaRepository<CarModel, Long> {

    CarModel findByModelName(String modelName);

}
