package com.szhq.iemp.reservation.repository;

import com.szhq.iemp.reservation.api.model.TpolicePrecinct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface PoliceRepository extends JpaRepository<TpolicePrecinct, String>, JpaSpecificationExecutor<TpolicePrecinct> {

    @Query(value = "select * from police_precinct where id=?1", nativeQuery = true)
    public TpolicePrecinct findByIdString(String id);

}
