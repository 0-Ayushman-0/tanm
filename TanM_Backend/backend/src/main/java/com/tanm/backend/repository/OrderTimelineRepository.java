package com.tanm.backend.repository;

import com.tanm.backend.entity.Order;
import com.tanm.backend.entity.OrderTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTimelineRepository extends JpaRepository<OrderTimeline, Long> {

    @Query("SELECT ot FROM OrderTimeline ot " +
           "WHERE ot.order = :order " +
           "ORDER BY ot.timestamp ASC")
    List<OrderTimeline> findByOrderOrderByTimestampAsc(@Param("order") Order order);
}
