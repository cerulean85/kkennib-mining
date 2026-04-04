package kkennib.net.mining.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberResponse {
  private Long no;
  private String email;
  private String name;
  private String picture;
  private String accountType;
  private String accessToken;
  private String refreshToken;

  public MemberResponse(Long no, String email, String accountType, String name, String picture, String accessToken, String refreshToken) {
    this.no = no;
    this.email = email;
    this.name = name;
    this.picture = picture;
    this.accountType = accountType;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
  public static MemberResponse empty() {
    return new MemberResponse(0L, "", "", "", "", "", "");
  }

  public static MemberResponse of(Long memberNo, String email, String accountType, String name, String picture) {
    return new MemberResponse(memberNo, email, accountType, name, picture, "", "");
  }
}