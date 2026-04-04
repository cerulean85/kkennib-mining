package kkennib.net.mining.member.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import kkennib.net.mining.dto.ServiceResponse;
import kkennib.net.mining.member.dto.MemberDto;
import kkennib.net.mining.member.dto.MemberResponse;
import kkennib.net.mining.member.entity.Member;
import kkennib.net.mining.member.entity.ResetToken;
import kkennib.net.mining.member.repository.MemberRepository;
import kkennib.net.mining.member.repository.ResetTokenRepository;
import kkennib.net.mining.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static kkennib.net.mining.util.ResponseFactory.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {


  @Autowired
  private JwtUtil jwtUtil;
  private final MemberRepository memberRepository;
  private final ResetTokenRepository tokenRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private Configuration freemarkerConfig;

  @Value("${server.domain}") private String serverDomain;

  public ServiceResponse<MemberResponse> getEmailMemberByPasswordAndAccountType(String email, String password, String accountType) {
    Optional<Member> optionalMember = memberRepository.findByEmailAndAccountType(email, accountType);
    if (optionalMember.isEmpty()) {
      return createErrorResponse("Member not found or invalid credentials");
    }

    Member member = optionalMember.get();
    if (!passwordEncoder.matches(password, member.getPassword())) { // 암호화된 비밀번호 비교
      return createErrorResponse("Invalid credentials");
    }

    MemberResponse memberResponse = MemberResponse.of(
            member.getNo(),
            member.getEmail(),
            member.getAccountType(),
            member.getName(),
            member.getPicture());
    memberResponse.setAccessToken(this.getAccessToken(email));
    memberResponse.setRefreshToken(this.getRefreshToken(email));

    return createSuccessResponse(memberResponse);
  }

  public MemberDto getEmailMemberByAccountType(String email, String accountType) {
    Optional<Member> optionalMember = memberRepository.findByEmailAndAccountType(email, accountType);
    if (optionalMember.isEmpty()) {
      return MemberDto.empty();
    }

    Member member = optionalMember.get();
    return MemberDto.of(member.getNo(), member.getEmail(), member.getAccountType(), member.getName(), member.getPicture());
  }

  public ServiceResponse<Boolean> validatePasswordResetToken(String token) {

    Optional<ResetToken> optResetToken = tokenRepository.findByToken(token);
    if (optResetToken.isEmpty()) {
      return createSuccessResponse(false);
    }

    ResetToken resetToken = optResetToken.get();
    if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      resetToken.setUsed(true);
      tokenRepository.save(resetToken);
      return createSuccessResponse(false);
    }

    return createSuccessResponse(true);
  }

  public ServiceResponse<Boolean> updatePassword(String token, String newPassword) {
    ServiceResponse<Boolean> tokenResponse = validatePasswordResetToken(token);
    if (!tokenResponse.isSuccess()) {
      return createErrorResponse("Invalid or expired token");
    }

    Long memberNo = tokenRepository.findByToken(token)
            .map(ResetToken::getUserId)
            .orElse(0L);

    if (memberNo == 0L) {
      return createErrorResponse("Member number not found for the provided token");
    }

    ServiceResponse<Boolean> response = createResponse(false);
    Optional<Member> optMember = memberRepository.findById (memberNo);
    if (optMember.isEmpty()) {
      response.setMessage("Member not found");
      return response;
    }

    if (newPassword == null || newPassword.isBlank()) {
      response.setMessage("Password cannot be empty");
      return response;
    }

    Member member = optMember.get();
    String encryptedPassword = passwordEncoder.encode(newPassword); // 비밀번호 암호화
    member.setPassword(encryptedPassword); // 비밀번호 업데이트
    memberRepository.save(member);

    ResetToken resetToken = tokenRepository.findByToken(token).get();
    resetToken.setUsed(true);
    tokenRepository.save(resetToken);

    response.setData(true);
    return response;
  }

  public ServiceResponse<Boolean> updateName(String email, String name) {
    Optional<Member> optMember = memberRepository.findByEmail(email);
    if (optMember.isEmpty()) {
      return createErrorResponse("Member not found");
    }

    Member member = optMember.get();
    if (name != null && !name.isBlank()) {
      member.setName(name);
      memberRepository.save(member); // DB 업데이트
      return createSuccessResponse(true);
    }

    return createErrorResponse("Name cannot be empty");
  }

  public ServiceResponse<Boolean> updateMemberPicture(Long memberNo, String picture) {
    Optional<Member> optMember = memberRepository.findById(memberNo);
    if (optMember.isEmpty()) {
      return createErrorResponse("Member not found");
    }

    Member member = optMember.get();
    if (picture != null && !picture.isBlank()) {
      member.setPicture(picture);
    }

    memberRepository.save(member); // DB 업데이트
    return createSuccessResponse(true);
  }

  public ServiceResponse<MemberResponse> createMember(
          String email, String password, String accountType, String name, String picture) {
    boolean joined = memberRepository.existsByEmail(email);
    if (joined) {
      return createErrorResponse("Member with this email already exists");
    }

    String encryptedPassword = passwordEncoder.encode(password);
    Member member = memberRepository.save(new Member(email, encryptedPassword, accountType, name, picture));
    if (member.getNo() == 0L) {
      return createErrorResponse("Failed to create member");
    }
    MemberDto memberDto = MemberDto.of(
            member.getNo(),
            member.getEmail(), member.getAccountType(), member.getName(), member.getPicture());
    memberDto.setAccessToken(this.getAccessToken(email));
    memberDto.setRefreshToken(this.getRefreshToken(email));
    MemberResponse memberResponse = new MemberResponse(
            memberDto.getNo(),
            memberDto.getEmail(),
            memberDto.getName(),
            memberDto.getPicture(),
            memberDto.getAccountType(),
            memberDto.getAccessToken(),
            memberDto.getRefreshToken());
    return createSuccessResponse(memberResponse);
  }

  public String getResetToken(Long memberNo) {
    String token = UUID.randomUUID().toString();
    Optional<ResetToken> existingToken = tokenRepository.findByUserId(memberNo);

    if (existingToken.isPresent()) {
      ResetToken resetToken = existingToken.get();
      resetToken.setToken(token);
      resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
      resetToken.setUsed(false);
      tokenRepository.save(resetToken); // UPDATE
    } else {
      tokenRepository.save(new ResetToken(memberNo, token, LocalDateTime.now().plusHours(1), false)); // INSERT
    }

    return token;
  }

  public String getResetPasswordPageHtmlContent(String token) {
    Map<String, Object> model = new HashMap<>();
    model.put("logoUrl", serverDomain + "/images/icon/logo_title.svg");
    model.put("resetUrl", serverDomain + "/update-pwd?token=" + token);
    model.put("contactUrl", serverDomain + "/contact");
    return this.processTemplate("reset-password.ftl", model);
  }

  public String getContactUsPageHtmlContent(String name, String email, String message) {
    Map<String, Object> model = new HashMap<>();
    model.put("logoUrl", serverDomain + "/images/icon/logo_title.svg");
    model.put("name", name);
    model.put("email", email);
    model.put("message", message);
    return this.processTemplate("contact-us.ftl", model);
  }

  public String getResetPasswordPageTextContent() {
    return this.loadTemplate("reset-password.txt");
  }

  public String getContactUsPageTextContent() {
    return this.loadTemplate("contact-us.txt");
  }

  private String getAccessToken(String email) {
    return Optional.ofNullable(email)
            .filter(e -> !e.isBlank())
            .map(jwtUtil::createAccessToken)
            .orElse("");
  }
  private String getRefreshToken(String email) {
    return Optional.ofNullable(email)
            .filter(e -> !e.isBlank())
            .map(jwtUtil::createRefreshToken)
            .orElse("");
  }

  private MemberDto getMemberFromEmailPasswordAccountType(String email, String password, String accountType) {
    Optional<Member> optionalMember = memberRepository.findByEmailAndPasswordAndAccountType(email, password, accountType);

    Member member = optionalMember.orElseGet(Member::new);
    if (member.getNo() == 0L) {
      return MemberDto.empty();
    }
    String accessToken = this.getAccessToken(email);
    String refreshToken = this.getRefreshToken(email);
    MemberDto memberDto = MemberDto.of(
            member.getNo(),
            member.getEmail(),
            member.getAccountType(),
            member.getName(),
            member.getPicture());

    memberDto.setAccessToken(accessToken);
    memberDto.setRefreshToken(refreshToken);
    return memberDto;
  }

  private String processTemplate(String templateName, Map<String, Object> model) {
    try {
      Template template = freemarkerConfig.getTemplate(templateName);
      StringWriter writer = new StringWriter();
      template.process(model, writer);
      return writer.toString();
    } catch(IOException | TemplateException e) {
      log.error("Error processing template: {}", e.getMessage());
      return "Something went wrong.";
    }
  }

  private String loadTemplate(String filename) {
    try {
      ClassPathResource resource = new ClassPathResource("templates/" + filename);
      return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("Error processing template: {}", e.getMessage());
      return "Something went wrong.";
    }
  }



}
