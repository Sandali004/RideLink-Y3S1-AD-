package fare_payment_service.repository;

import fare_payment_service.entity.Fare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FareRepository extends JpaRepository<Fare, Long> {

}