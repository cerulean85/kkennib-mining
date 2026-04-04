package kkennib.net.mining.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


@Component
public class JwtUtil {
  private final String secret = getFirstDayOfThisMonthAtMidnightUTC();
  public static String getFirstDayOfThisMonthAtMidnightUTC() {
    ZonedDateTime zdt = LocalDate.now()
            .withDayOfMonth(1)
            .atStartOfDay(ZoneOffset.UTC);

    return zdt.format(DateTimeFormatter.ISO_INSTANT);
  }

  public String createAccessToken(String email) {
    return JWT.create()
            .withSubject(email)
            .withExpiresAt(Date.from(Instant.now().plusSeconds(60 * 60 * 2)))
            .sign(Algorithm.HMAC256(secret));
  }

  public String createRefreshToken(String email) {
    return JWT.create()
            .withSubject(email)
            .withExpiresAt(Date.from(Instant.now().plusSeconds(60 * 60 * 24 * 7)))
            .sign(Algorithm.HMAC256(secret));
  }

  public String verifyToken(String token) {
    return JWT.require(Algorithm.HMAC256(secret)).build().verify(token).getSubject();
  }
}