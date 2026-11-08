package com.limpacao.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remetente;

    @Value("${limpacao.mail.nome-remetente:LimpAção}")
    private String nomeRemetente;

    public void enviarCodigoRecuperacaoSenha(String destinatario, String nome, String codigo, int validadeMinutos) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(String.format("%s <%s>", nomeRemetente, remetente));
        mensagem.setTo(destinatario);
        mensagem.setSubject("Código para redefinir sua senha");
        mensagem.setText(String.format("""
                Olá, %s!

                Recebemos uma solicitação para redefinir a senha da sua conta no %s.

                Seu código de verificação é: %s

                Ele é válido por %d minutos. Se você não fez essa solicitação, ignore este e-mail; sua senha continuará a mesma.
                """, nome, nomeRemetente, codigo, validadeMinutos));

        try {
            mailSender.send(mensagem);
        } catch (MailException ex) {
            log.error("Falha ao enviar e-mail de recuperação de senha para {}", destinatario, ex);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível enviar o e-mail de recuperação. Tente novamente mais tarde.");
        }
    }
}
