package com.smarturban.backend.repository;

import com.smarturban.backend.entity.LocationRoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRoutingRuleRepository extends JpaRepository<LocationRoutingRule, Long> {
    List<LocationRoutingRule> findByEnabledTrue();
}
