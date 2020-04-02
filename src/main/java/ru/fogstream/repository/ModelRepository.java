package ru.fogstream.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.CarModel;

@Repository
public interface ModelRepository extends CrudRepository<CarModel, Long> {

}
