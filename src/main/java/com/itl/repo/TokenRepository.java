package com.itl.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.itl.entities.TokenEntity;

@Component
@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, String>{

	public TokenEntity  findByToken(String  token);
}
