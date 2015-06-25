package jp.inc.forrest.aws.component;

import java.io.File;
import java.security.PublicKey;

import javax.annotation.Resource;

import jp.inc.forrest.aws.service.dto.Message;
import jp.inc.forrest.aws.util.MessageReadUtil;
import mockit.Expectations;
import mockit.Mocked;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.services.sns.util.SignatureChecker;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext_ut.xml" })
public class MessageSignatureComponentImplTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Resource
	private MessageSignatureComponent target;

	// SignatureCheckerをモックオブジェクトとして定義する
	@Mocked
	private SignatureChecker signatureChecker;

	@Before
	public void setup() {
		target = applicationContext.getBean(MessageSignatureComponent.class);
	}

	/**
	 * checkテスト
	 * 
	 * @throws Throwable
	 */
	@Ignore
	@Test
	public void checkTest() throws Throwable {

		new Expectations() {
			{
				// モックオブジェクトの振る舞いを定義する
				signatureChecker.verifyMessageSignature(anyString, (PublicKey) any);
				result = true;
			}
		};

		String message = FileUtils.readFileToString(new File("Notification.json"));
		Message msg = MessageReadUtil.readMessageFromJson(message);
		ObjectMapper mapper = new ObjectMapper();
		message = mapper.writeValueAsString(msg);
		PublicKey publicKey = target.getPublicKey(msg);

		target.check(message, publicKey);

	}

	@Test
	public void helloTest() {
		Assert.assertEquals("hello", "hello");
	}
}
