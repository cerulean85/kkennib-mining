package kkennib.net.mining.member.controller;

import kkennib.net.mining.dto.ServiceResponse;
import kkennib.net.mining.enums.AccountType;
import kkennib.net.mining.member.dto.MemberDto;
import kkennib.net.mining.member.dto.MemberResponse;
import kkennib.net.mining.member.service.EmailService;
import kkennib.net.mining.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static kkennib.net.mining.util.ResponseFactory.createErrorResponse;
import static kkennib.net.mining.util.ResponseFactory.createSuccessResponse;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberLogInController {

  private final MemberService memberService;
  private final EmailService emailService;

  @Value("${custom.email.contact}")
  private String contactEmail;

  @PostMapping("/log-in")
  public ServiceResponse<MemberResponse> login(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String password = request.get("password");
    String accountType = request.get("accountType");
    String picture = request.get("picture");
    ServiceResponse<MemberResponse> memberResponse =  memberService.getEmailMemberByPasswordAndAccountType(email, password, accountType);
    MemberResponse res = memberResponse.getData();
    if (!res.getPicture().equals(picture)) {
      memberService.updateMemberPicture(res.getNo(), picture);
    } else {
      log.warn("Name or picture mismatch during login for email: {}", email);
    }
    return memberResponse;
  }

  @PostMapping("/sign-up")
  public ServiceResponse<MemberResponse> signup(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String password = request.get("password");
    String accountType = request.get("accountType");
    String name = request.get("name");
    String picture = request.get("picture");
    return memberService.createMember(email, password, accountType, name, picture);
  }

  @GetMapping("/check-reset-token")
  public ServiceResponse<Boolean> checkResetToken(@RequestParam("token") String token) {
    if (token == null) {
      return createErrorResponse("Member number and token are required.");
    }
    return memberService.validatePasswordResetToken(token);
  }

  @PostMapping("/send-password-reset-email")
  public ServiceResponse<Boolean> sendTestEmail(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String accountType = AccountType.EMAIL.getValue();
    MemberDto memberDto = memberService.getEmailMemberByAccountType(email, accountType);
    Long memberNo = memberDto.getNo();
    if (memberNo == 0L) {
      return createErrorResponse("There is no member registered with the given email.");
    }

    String token = memberService.getResetToken(memberNo);
    String htmlContent = memberService.getResetPasswordPageHtmlContent(token);
    String textContent = memberService.getResetPasswordPageTextContent();
    try {
      emailService.sendEmail(
              email,
              "Reset your Stock DEV password",
              htmlContent,
              textContent
      );
    } catch (Exception e) {
      log.error("Error processing template: {}", e.getMessage());
      return createErrorResponse("Email address is not verified.");
    }
    return createSuccessResponse(true);
  }

  @PutMapping("/update-password")
  public ServiceResponse<Boolean> updatePassword(@RequestBody Map<String, String> request) {
    String token = request.get("token");
    String newPassword = request.get("password");
    if (token == null || newPassword == null) {
      return createErrorResponse("Token and new password are required.");
    }
    return memberService.updatePassword(token, newPassword);
  }

  @PutMapping("/update-name")
  public ServiceResponse<Boolean> updateName(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String name = request.get("name");
    return memberService.updateName(email, name);
  }

  @PostMapping("/contact-us")
  public ServiceResponse<Boolean> contactUs(@RequestBody Map<String, String> request) {
    String name = request.get("name");
    String email = request.get("email");
    String message = request.get("message");

    String htmlContent = memberService.getContactUsPageHtmlContent(name, email, message);
    String textContent = memberService.getContactUsPageTextContent();
    try {
      emailService.sendEmail(
              contactEmail,
              "A user has a question about Stock DEV.",
              htmlContent,
              textContent
      );
    } catch (Exception e) {
      log.error("Error processing template: {}", e.getMessage());
      return createErrorResponse("Email address is not verified.");
    }
    return createSuccessResponse(true);
  }

}
