package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.SubgroupComp;

@Repository
public interface SubgroupRepository extends JpaRepository<SubgroupComp, Long> {

    SubgroupComp findBySubgroupName(String subgroupName);

}
