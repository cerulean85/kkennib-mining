package kkennib.net.mining.member.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

  private final SesClient sesClient;

  public EmailService(SesClient sesClient) {
    this.sesClient = sesClient;
  }

  public void sendEmail(String to, String subject, String htmlBody, String textBody) {
    SendEmailRequest emailRequest = SendEmailRequest.builder()
            .source("zhwan85@kkennib.net")  // 인증된 이메일 주소
            .destination(Destination.builder()
                    .toAddresses(to)
                    .build())
            .message(Message.builder()
                    .subject(Content.builder().data(subject).charset("UTF-8").build())
                    .body(Body.builder()
                            .text(Content.builder().data(textBody).charset("UTF-8").build())
                            .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                            .build())
                    .build())
            .build();

    sesClient.sendEmail(emailRequest);
  }
}