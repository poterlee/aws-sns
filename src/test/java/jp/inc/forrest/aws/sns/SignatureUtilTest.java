package jp.inc.forrest.aws.sns;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;

import javax.annotation.Resource;

import jp.inc.forrest.aws.component.MessageSignatureComponent;
import jp.inc.forrest.aws.service.dto.Message;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.services.sns.util.SignatureChecker;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext_ut.xml" })
public class SignatureUtilTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Resource
	private MessageSignatureComponent messageSignatureComponent;

	@Before
	public void setup() {
		messageSignatureComponent = applicationContext.getBean(MessageSignatureComponent.class);
	}

	/**
	 * com.amazonaws.services.sns.util.SignatureChecker#
	 * parseJSONでRuntimeExceptionが発生する場合の回避方法をテスト
	 *
	 * @throws Throwable
	 * @see SignatureChecker
	 */
	@Test
	public void jsonParseTest() throws Throwable {

		// テストデータを読み込む（Notification.jsonはSNSから発行して、別途用意する必要があります。）
		String message = FileUtils.readFileToString(new File("Notification.json"));

		Message msg = readMessageFromJson(message);
		// メッセージにMessageAttributesが含まれている場合、以下のメソッド実行でRuntimeExceptionが発生するため、
		// メッセージオブジェクトからメッセージを再生成する。
		ObjectMapper mapper = new ObjectMapper();
		message = mapper.writeValueAsString(msg);
		PublicKey publicKey = messageSignatureComponent.getPublicKey(msg);
		messageSignatureComponent.check(message, publicKey);
	}

	private Message readMessageFromJson(String json) {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {

			Message msg = objectMapper.readValue(json, Message.class);

			return msg;

		} catch (IOException e) {

			return null;
		}

	}

}
