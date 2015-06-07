package jp.inc.forrest.aws.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import jp.inc.forrest.aws.service.dto.Message;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * SNSトピックのエンドポイント実装サンプル（コントローラー）
 * 
 * @author kuhubgit
 *
 */
@Controller
public class MintTopicController {

	// メッセージタイプヘッダー文字列
	private static final String MESSAGE_TYPE = "x-amz-sns-message-type";

	private static final Logger logger = LoggerFactory.getLogger(MintTopicController.class);

	// POST Content-Type: text/plain
	@RequestMapping(value = "/topic", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String subscribe(HttpServletRequest httpServletRequest) {

		try {
			String messagetype = httpServletRequest.getHeader(MESSAGE_TYPE);
			if (messagetype == null)
				return "message-type is null.";

			@SuppressWarnings("resource")
			Scanner inputScanner = new Scanner(httpServletRequest.getInputStream());
			StringBuilder builder = new StringBuilder();
			while (inputScanner.hasNextLine()) {
				builder.append(inputScanner.nextLine());
			}
			Message msg = readMessageFromJson(builder.toString());

			// The signature is based on SignatureVersion 1.
			if ("1".equals(msg.getSignatureVersion())) {
				if (isMessageSignatureValid(msg))
					logger.info(">>Signature verification succeeded");
				else {
					logger.info(">>Signature verification failed");
					throw new SecurityException("Signature verification failed.");
				}
			} else {
				logger.info(">>Unexpected signature version. Unable to verify signature.");
				throw new SecurityException("Unexpected signature version. Unable to verify signature.");
			}

			// modify to switch
			if (messagetype.equals("Notification")) {
				// TODO: Do something with the Message and Subject.
				// Just log the subject (if it exists) and the message.
				String logMsgAndSubject = ">>Notification received from topic " + msg.getTopicArn();
				if (msg.getSubject() != null)
					logMsgAndSubject += " Subject: " + msg.getSubject();
				logMsgAndSubject += " Message: " + msg.getMessage();
				logger.info(logMsgAndSubject);

			} else if (messagetype.equals("SubscriptionConfirmation")) {
				// TODO: You should make sure that this subscription is from the
				// topic you expect. Compare topicARN to your list of topics
				// that you want to enable to add this endpoint as a
				// subscription.

				// Confirm the subscription by going to the subscribeURL
				// location
				// and capture the return value (XML message body as a string)
				@SuppressWarnings("resource")
				Scanner subscribeScanner = new Scanner(new URL(msg.getSubscribeURL()).openStream());
				StringBuilder sb = new StringBuilder();
				while (subscribeScanner.hasNextLine()) {
					sb.append(subscribeScanner.nextLine());
				}
				logger.info(">>Subscription confirmation (" + msg.getSubscribeURL() + ") Return value: "
						+ sb.toString());
				// TODO: Process the return value to ensure the endpoint is
				// subscribed.
			} else if (messagetype.equals("UnsubscribeConfirmation")) {
				// TODO: Handle UnsubscribeConfirmation message.
				// For example, take action if unsubscribing should not have
				// occurred.
				// You can read the SubscribeURL from this message and
				// re-subscribe the endpoint.
				logger.info(">>Unsubscribe confirmation: " + msg.getMessage());
			} else {
				// TODO: Handle unknown message type.
				logger.info(">>Unknown message type.");
			}

			logger.info(">>Done processing message: " + msg.getMessageId());

		} catch (Exception e) {
			// XXX: Handle more better.
			logger.error(e.getMessage(), e);
		}

		return "ok";
	}

	/**
	 * JSON文字列を{@link Message}オブジェクトへ変換する。
	 * 
	 * @param json
	 * @return
	 */
	private Message readMessageFromJson(String json) {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {

			Message msg = objectMapper.readValue(json, Message.class);
			logger.info(msg.toString());

			return msg;

		} catch (IOException e) {

			logger.error(e.getMessage(), e);
			return null;
		}

	}

	private static byte[] getMessageBytesToSign(Message msg) {
		byte[] bytesToSign = null;
		if (msg.getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}

	// Build the string to sign for Notification messages.
	public static String buildNotificationStringToSign(Message msg) {
		String stringToSign = null;

		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		if (msg.getSubject() != null) {
			stringToSign += "Subject\n";
			stringToSign += msg.getSubject() + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	// Build the string to sign for SubscriptionConfirmation
	// and UnsubscribeConfirmation messages.
	public static String buildSubscriptionStringToSign(Message msg) {
		String stringToSign = null;
		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += msg.getSubscribeURL() + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "Token\n";
		stringToSign += msg.getToken() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	private static boolean isMessageSignatureValid(Message msg) {
		try {
			URL url = new URL(msg.getSigningCertURL());
			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(inStream);
			inStream.close();

			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(getMessageBytesToSign(msg));
			return sig.verify(Base64.decodeBase64(msg.getSignature()));
		} catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);
		}
	}

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	@ResponseBody
	public Message hello() {
		return new Message();
	}

}
