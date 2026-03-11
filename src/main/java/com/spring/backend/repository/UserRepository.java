package com.spring.backend.repository;

import com.spring.backend.entity.UserEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository
    extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

  Optional<UserEntity> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);

  boolean existsByCardId(String cardId);

  long countByIsActiveIsTrue();

  static Specification<UserEntity> search(
      String name, String email, String phone, String cardId, String username) {

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (name != null) {
        predicates.add(cb.like(root.get("name"), "%" + name + "%"));
      }

      if (email != null) {
        predicates.add(cb.like(root.get("email"), "%" + email + "%"));
      }

      if (phone != null) {
        predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
      }

      if (cardId != null) {
        predicates.add(cb.like(root.get("cardId"), "%" + cardId + "%"));
      }

      if (username != null) {
        predicates.add(cb.like(root.get("username"), "%" + username + "%"));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
