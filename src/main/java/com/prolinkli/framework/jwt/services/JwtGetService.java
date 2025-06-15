package com.prolinkli.framework.jwt.services;

import com.prolinkli.core.app.db.model.generated.JwtTokenDb;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.jwt.model.AuthToken;

import org.springframework.stereotype.Service;

@Service
public class JwtGetService {

	private final Dao<JwtTokenDb, Long> dao;

	public JwtGetService(DaoFactory daoFactory) {
		this.dao = daoFactory.getDao(JwtTokenDb.class, Long.class);
	}

	public AuthToken getJwtToken(Long userId) {
		JwtTokenDb jwtTokenDb = dao.select(userId);
		if (jwtTokenDb == null) {
			return null; // or throw an exception if preferred
		}

		return new AuthToken.AuthTokenBuilder()
				.accessToken(jwtTokenDb.getAccessToken())
				.refreshToken(jwtTokenDb.getRefreshToken())
				.build();
	}

}
