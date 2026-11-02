package in.aerosaga.repository;

import in.aerosaga.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    Optional<Mission> findByMissionId(String missionId);

    List<Mission> findByDroneId(String droneId);

    List<Mission> findByStatus(String status);

    List<Mission> findByDroneIdAndStatus(String droneId, String status);
}