package jp.inc.forrest.aws.service;

import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.util.Scanner;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import jp.inc.forrest.aws.component.MessageSignatureComponent;
import jp.inc.forrest.aws.service.dto.Message;

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

	@Resource
	private MessageSignatureComponent messageSignatureComponent;

	// POST Content-Type: text/plain
	@RequestMapping(value = "/topic", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String subscribe(HttpServletRequest httpServletRequest) {

		try {
			String messagetype = httpServletRequest.getHeader(MESSAGE_TYPE);

			if (messagetype == null) {
				throw new IllegalAccessError(MESSAGE_TYPE + "　が存在しない不正なメッセージを受信しました。");
			}

			@SuppressWarnings("resource")
			Scanner inputScanner = new Scanner(httpServletRequest.getInputStream());
			StringBuilder builder = new StringBuilder();
			while (inputScanner.hasNextLine()) {
				builder.append(inputScanner.nextLine());
			}

			// メッセージ
			String message = builder.toString();
			// メッセージオブジェクト
			Message msg = readMessageFromJson(message);

			// メッセージにMessageAttributesが含まれている場合、以下のメソッド実行でRuntimeExceptionが発生するため、
			// メッセージオブジェクトからメッセージを再生成する。
			// com.amazonaws.services.sns.util.SignatureChecker#parseJSON
			ObjectMapper mapper = new ObjectMapper();
			message = mapper.writeValueAsString(msg);

			// 公開鍵の取得
			PublicKey publicKey = messageSignatureComponent.getPublicKey(msg);

			// 電子署名の検証
			boolean verification = messageSignatureComponent.check(message, publicKey);

			if (verification) {

				// サブスクライブ確認の場合のみコールバック処理
				if (messagetype.equals("SubscriptionConfirmation")) {
					@SuppressWarnings("resource")
					Scanner subscribeScanner = new Scanner(new URL(msg.getSubscribeURL()).openStream());
					StringBuilder sb = new StringBuilder();
					while (subscribeScanner.hasNextLine()) {
						sb.append(subscribeScanner.nextLine());
					}
					logger.info(" >> Subscription confirmation (" + msg.getSubscribeURL() + ") Return value: " + sb.toString());
				}

				// TODO: ライフサイクルフックの実装
				logger.info(msg.toString());

			} else {
				throw new IllegalAccessError("電子署名の検証に失敗しました。 \n" + message);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return "ok";
	}

	/**
	 * ヘルスチェック用API
	 *
	 * @return
	 */
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	@ResponseBody
	public Message hello() {
		messageSignatureComponent.hello("forrestinc");
		return new Message();
	}

	/**
	 * JSON文字列を{@link Message}オブジェクトへ変換する。
	 *
	 * @param json
	 * @return
	 */
	private Message readMessageFromJson(String json) {

		ObjectMapper objectMapper = new ObjectMapper();

		// マッピングされるオブジェクトに未定義な属性は無視する設定
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

}
