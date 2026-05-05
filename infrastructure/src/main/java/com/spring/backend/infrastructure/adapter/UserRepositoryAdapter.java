package com.spring.backend.infrastructure.adapter;

import com.spring.backend.domain.user.User;
import com.spring.backend.domain.user.UserRepository;
import com.spring.backend.infrastructure.mapper.UserMapper;
import com.spring.backend.infrastructure.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // UserJpaRepository does not have findByEmail; delegate via Specification search
        // that filters on email field. Use JPA findAll with spec for exact match.
        return jpaRepository.findAll(
            (root, query, cb) -> cb.equal(root.get("email"), email)
        ).stream().findFirst().map(mapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        var entity = mapper.toEntity(user);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
