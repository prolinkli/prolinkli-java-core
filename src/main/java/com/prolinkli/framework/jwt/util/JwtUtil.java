package com.prolinkli.framework.jwt.util;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.security.Keys;

public class JwtUtil {

	public static Key getHmacShaKey(String key) {
		return Keys.hmacShaKeyFor(key.getBytes());
	}

	public static SecretKey getSecretKey(String key) {
		return Keys.hmacShaKeyFor(key.getBytes());
	}

	public static Date getExpirationDate(long expiration) {
		return new Date(System.currentTimeMillis() + expiration * 1000);
	}

	public static boolean didExpire(Date expirationDate) {
		return expirationDate.before(new Date(System.currentTimeMillis()));
	}

}
