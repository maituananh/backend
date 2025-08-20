package com.spring.backend.repository;

import com.spring.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepositoryUser extends JpaRepository<UserEntity, Long> {

    UserEntity findByEmail(String email);

   // @Query("SELECT u FROM UserEntity u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserEntity> findByNameLikeIgnoreCase(String name);

    UserEntity findByPhone(String phone);

    UserEntity fineByIdCard(String card_id);

}
