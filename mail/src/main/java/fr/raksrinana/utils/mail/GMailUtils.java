package fr.raksrinana.utils.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class GMailUtils{
	private static final Logger LOGGER = LoggerFactory.getLogger(GMailUtils.class);
	private static final String GMAIL_SMTP_HOST = "smtp.gmail.com";
	
	public static void sendGMail(@Nonnull String user, @Nonnull String password, @Nonnull String from, @Nonnull String to, @Nonnull String object, @Nonnull String body) throws MessagingException, UnsupportedEncodingException{
		MailUtils.sendMail(getGMailSession(user, password), user, from, to, object, body);
	}
	
	@Nonnull
	public static Session getGMailSession(@Nonnull String user, @Nonnull String password){
		Properties properties = System.getProperties();
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.host", GMAIL_SMTP_HOST);
		properties.put("mail.smtp.port", "587");
		return Session.getInstance(properties, new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication(){
				return new PasswordAuthentication(user, password);
			}
		});
	}
	
	@Nonnull
	public static GMailFetcher fetchGMailFolder(@Nonnull String user, @Nonnull String password, @Nonnull String folder, Consumer<MessageCountEvent> callback) throws IllegalStateException, MessagingException{
		return new GMailFetcher(getGMailStore(user, password), folder, callback);
	}
	
	@Nonnull
	public static Store getGMailStore(@Nonnull String user, @Nonnull String password) throws MessagingException{
		Store store = getGMailSession(user, password).getStore("imaps");
		store.connect(GMAIL_SMTP_HOST, user, password);
		return store;
	}
	
	@Nonnull
	public static GMailFetcher fetchGMailFolder(@Nonnull String user, @Nonnull String password, @Nonnull String folder, @Nullable ExecutorService executor, @Nonnull Consumer<MessageCountEvent> callback) throws IllegalStateException, MessagingException{
		return new GMailFetcher(getGMailStore(user, password), folder, executor, callback);
	}
	
	public static boolean forward(@Nonnull String user, @Nonnull String password, @Nonnull String fromName, @Nonnull String to, @Nonnull String toName, @Nonnull Message message){
		return forward(user, password, fromName, to, toName, message, "Fwd: ", "");
	}
	
	public static boolean forward(@Nonnull String user, @Nonnull String password, @Nonnull String fromName, @Nonnull String to, @Nonnull String toName, @Nonnull Message message, @Nonnull String subjectPrefix, @Nonnull String header){
		return forward(getGMailSession(user, password), user, fromName, to, toName, message, subjectPrefix, header);
	}
	
	public static boolean forward(@Nonnull Session session, @Nonnull String user, @Nonnull String fromName, @Nonnull String to, @Nonnull String toName, @Nonnull Message message, @Nonnull String subjectPrefix, @Nonnull String header){
		try{
			Message forwardMessage = new MimeMessage(session);
			forwardMessage.setSubject(subjectPrefix + (message.getSubject() == null ? "" : message.getSubject()));
			forwardMessage.setFrom(new InternetAddress(user, fromName));
			forwardMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to, toName));
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(header);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			try{
				Object content = message.getContent();
				if(content instanceof String){
					BodyPart parts = new MimeBodyPart();
					parts.setText(String.valueOf(content));
					multipart.addBodyPart(parts);
				}
				else if(content instanceof Multipart){
					Multipart parts = (Multipart) content;
					for(int i = 0; i < parts.getCount(); i++){
						multipart.addBodyPart(parts.getBodyPart(i));
					}
				}
			}
			catch(MessagingException | IOException e){
				e.printStackTrace();
			}
			forwardMessage.setContent(multipart);
			Transport.send(forwardMessage);
			return true;
		}
		catch(Exception e){
			LOGGER.warn("Failed to forward message", e);
			return false;
		}
	}
}