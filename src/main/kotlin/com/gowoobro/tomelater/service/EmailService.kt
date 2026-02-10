package com.gowoobro.tomelater.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service


@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${MAIL_USERNAME}") private val fromEmail: String
) {

    fun sendEmail(to: String, subject: String, text: String, isHtml: Boolean = false) {
        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, "utf-8")
        
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(text, isHtml)
        helper.setFrom(fromEmail)
        
        mailSender.send(mimeMessage)
    }

    fun sendVerificationCode(to: String, code: String) {
        val subject = "[To Me, Later] 회원가입 인증 코드입니다."
        val htmlContent = """
            <div style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; max-width: 500px; margin: 0 auto; padding: 40px 20px; border: 1px solid #f0f0f0; border-radius: 12px; background-color: #ffffff;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #4f46e5; margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">To Me, Later</h1>
                    <p style="color: #6b7280; font-style: italic; margin-top: 8px;">Start your journey to the future self.</p>
                </div>
                
                <div style="background-color: #f9fafb; padding: 30px; border-radius: 8px; text-align: center; border: 1px dashed #e5e7eb;">
                    <p style="font-size: 16px; color: #374151; margin-bottom: 20px; line-height: 1.6;">
                        안녕하세요!<br>
                        투미레이터 회원가입을 신청해 주셔서 감사합니다.<br>
                        아래의 6자리 인증 코드를 입력하여 가입을 완료해 주세요.
                    </p>
                    
                    <div style="font-size: 36px; font-weight: 800; color: #4f46e5; letter-spacing: 12px; margin: 25px 0;">
                        $code
                    </div>
                    
                    <p style="font-size: 13px; color: #9ca3af; margin-bottom: 0;">
                        이 코드는 5분 동안 유효합니다.
                    </p>
                </div>
                
                <div style="margin-top: 35px; border-top: 1px solid #f3f4f6; pt-20px; text-align: center;">
                    <p style="font-size: 12px; color: #9ca3af; margin-top: 20px;">
                        본 메일은 발신 전용입니다. 문의 사항은 고객센터를 이용해 주세요.<br>
                        © ${java.time.Year.now().value} To Me, Later. All rights reserved.
                    </p>
                </div>
            </div>
        """.trimIndent()
        
        sendEmail(to, subject, htmlContent, true)
    }

    fun sendTimeCapsuleEmail(to: String, nickname: String, question: String, answer: String, date: String) {
        val subject = "[To Me, Later] 1년 전의 나로부터 편지가 도착했습니다."
        val htmlContent = """
            <div style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px; border: 1px solid #f0f0f0; border-radius: 12px; background-color: #ffffff;">
                <div style="text-align: center; margin-bottom: 40px;">
                    <h1 style="color: #4f46e5; margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">To Me, Later</h1>
                    <p style="color: #6b7280; font-style: italic; margin-top: 8px;">A letter from your past self.</p>
                </div>
                
                <div style="background-color: #f9fafb; padding: 30px; border-radius: 12px; border: 1px solid #e5e7eb;">
                    <p style="font-size: 16px; color: #374151; margin-bottom: 24px; line-height: 1.6;">
                        안녕하세요, <strong>$nickname</strong>님.<br>
                        1년 전 오늘, 당신이 남긴 이야기가 도착했습니다.
                    </p>
                    
                    <div style="margin-bottom: 30px;">
                        <span style="display: inline-block; background-color: #e0e7ff; color: #4338ca; font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 16px; margin-bottom: 12px;">QUESTION</span>
                        <h3 style="margin: 0; color: #1f2937; font-size: 18px; font-weight: 700; line-height: 1.4;">$question</h3>
                        <p style="margin: 8px 0 0; color: #9ca3af; font-size: 13px;">$date 작성됨</p>
                    </div>
                    
                    <div style="background-color: #ffffff; padding: 25px; border-radius: 8px; border-left: 4px solid #4f46e5; box-shadow: 0 2px 4px rgba(0,0,0,0.05);">
                        <p style="margin: 0; color: #4b5563; font-size: 16px; line-height: 1.8; white-space: pre-wrap;">$answer</p>
                    </div>
                </div>
                
                <div style="text-align: center; margin-top: 40px;">
                    <a href="https://tomelater.gowoobro.com" style="display: inline-block; background-color: #4f46e5; color: #ffffff; text-decoration: none; padding: 14px 28px; border-radius: 8px; font-weight: 600; font-size: 15px; transition: background-color 0.2s;">
                        투미레이터에서 확인하기
                    </a>
                </div>
                
                <div style="margin-top: 40px; border-top: 1px solid #f3f4f6; padding-top: 20px; text-align: center;">
                    <p style="font-size: 12px; color: #9ca3af; margin: 0;">
                        본 메일은 발신 전용입니다.<br>
                        © ${java.time.Year.now().value} To Me, Later. All rights reserved.
                    </p>
                </div>
            </div>
        """.trimIndent()
        
        sendEmail(to, subject, htmlContent, true)
    }

    fun sendPasswordResetCode(to: String, code: String) {
        val subject = "[To Me, Later] 비밀번호 재설정 인증 코드입니다."
        val htmlContent = """
            <div style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; max-width: 500px; margin: 0 auto; padding: 40px 20px; border: 1px solid #f0f0f0; border-radius: 12px; background-color: #ffffff;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #4f46e5; margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">To Me, Later</h1>
                    <p style="color: #6b7280; font-style: italic; margin-top: 8px;">Reset your password securely.</p>
                </div>
                
                <div style="background-color: #fff1f2; padding: 30px; border-radius: 8px; text-align: center; border: 1px dashed #fecdd3;">
                    <p style="font-size: 16px; color: #9f1239; margin-bottom: 20px; line-height: 1.6;">
                        비밀번호 재설정을 요청하셨습니다.<br>
                        아래의 인증 코드를 입력하여 비밀번호를 재설정해 주세요.
                    </p>
                    
                    <div style="font-size: 36px; font-weight: 800; color: #be123c; letter-spacing: 12px; margin: 25px 0;">
                        $code
                    </div>
                    
                    <p style="font-size: 13px; color: #be123c; margin-bottom: 0;">
                        이 코드는 5분 동안 유효합니다.<br>
                        본인이 요청하지 않았다면 이 메일을 무시하세요.
                    </p>
                </div>
                
                <div style="margin-top: 35px; border-top: 1px solid #f3f4f6; pt-20px; text-align: center;">
                    <p style="font-size: 12px; color: #9ca3af; margin-top: 20px;">
                        본 메일은 발신 전용입니다.<br>
                        © ${java.time.Year.now().value} To Me, Later. All rights reserved.
                    </p>
                </div>
            </div>
        """.trimIndent()
        
        sendEmail(to, subject, htmlContent, true)
    }
}


