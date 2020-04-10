package ru.fogstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fogstream.entity.GroupComp;

@Repository
public interface GroupRepository extends JpaRepository<GroupComp, Long> {

    GroupComp findByGroupName(String groupName);

}
