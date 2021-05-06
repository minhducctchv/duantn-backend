package duantn.backend.component;

import duantn.backend.authentication.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class MailSender {
    final
    JavaMailSender javaMailSender;
    final
    UploadFile uploadFile;

    public MailSender(JavaMailSender javaMailSender, UploadFile uploadFile) {
        this.javaMailSender = javaMailSender;
        this.uploadFile = uploadFile;
    }

    public void send(String to,
                     String subject, String body, String note, String...attachments) throws CustomException{
        try {
            String from="minhduc102017@gmail.com";

            //create mail
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            //user class helper
            MimeMessageHelper mimeMessageHelper=
                    new MimeMessageHelper(mimeMailMessage, true);
            mimeMessageHelper.setFrom(from, from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setReplyTo(from, from);
            mimeMessageHelper.setSubject(subject);
            //mimeMessageHelper.setText(body,true);

            String src="https://avatars.githubusercontent.com/u/65065713?v=4";
            String content="<table style=\"width: 100%\">\n" +
                    "\t<caption><h1>"+subject+"</h1></caption>\n" +
                    "\t<tbody>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td style=\"width: 100%\">"+body+"</td>\n" +
                    "\t\t</tr>\n" +
                    "\t\t<tr><td style=\"width: 100%; text-align: center;\">--------------</td></tr>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td style=\"width: 100%; text-align: right;\"><i style=\"color: red\">"+note+"</i></td>\n" +
                    "\t\t</tr>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td style=\"width: 100%; text-align: center;\">\n" +
                    "\t\t\t<br/><br/><br/><br/><br/>\n" +
                    "\t\t\t<b>Website nhà trọ thanh xuân</b><br/>\n" +
                    "\t\t\t<img style=\"height: 120px\" src=\""+src+"\"/>\n" +
                    "\t\t\t<p style=\"color: blue\"><b>SĐT:</b> 1900.100có</p>\n" +
                    "\t\t\t<p style=\"color: blue\"><b>Email:</b> minhduc102017@gmail.com</p>\n" +
                    "\t\t\t</td>\n" +
                    "\t\t</tr>\n" +
                    "\t</tbody>\n" +
                    "</table>";

            mimeMailMessage.setContent(content,"text/html; charset=utf-8");
            if(attachments.length>0){
                for(String attachment: attachments){
                    mimeMessageHelper.addAttachment(attachment, uploadFile.load(attachment));
                }
            }
            javaMailSender.send(mimeMailMessage);
        }catch (Exception e){
            e.printStackTrace();
            throw new CustomException("Gửi mail thất bại");
        }
    }
}
