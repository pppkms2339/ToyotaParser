package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.BodyBrand;

@Repository
public interface BodyBrandRepository extends JpaRepository<BodyBrand, Long> {

}
