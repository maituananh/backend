package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.ImageEntity;
import com.spring.backend.repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ImageRepositoryUT {

  @Autowired private ImageRepository imageRepository;
  @Autowired private TestEntityManager entityManager;

  @Test
  @DisplayName("ImageRepository should save and retrieve images")
  void saveAndRetrieve_Works() {
    ImageEntity image = ImageEntity.builder().fileName("test.png").build();
    ImageEntity saved = imageRepository.save(image);
    assertThat(saved.getId()).isNotNull();
    assertThat(imageRepository.findById(saved.getId())).isPresent();
  }
}
