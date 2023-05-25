package hit.repo;

import hit.model.EndpointHit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HitRepository extends JpaRepository<EndpointHit, Long> {



}