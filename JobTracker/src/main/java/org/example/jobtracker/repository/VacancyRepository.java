package org.example.jobtracker.repository;

import org.example.jobtracker.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    List<Vacancy> findByStatus(String status);

    List<Vacancy> findByCompanyContainingIgnoreCase(String company);

    Optional<Vacancy> findByLink(String link);

}

