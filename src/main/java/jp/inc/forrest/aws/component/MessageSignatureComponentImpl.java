package jp.inc.forrest.aws.component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.annotation.Resource;

import jp.inc.forrest.aws.service.dto.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sns.util.SignatureChecker;

/**
 * 電子署名検証コンポーネント実装クラス
 *
 * @author kuhubgit
 *
 */
@Component
public class MessageSignatureComponentImpl implements MessageSignatureComponent {

	private static final Logger logger = LoggerFactory.getLogger(MessageSignatureComponentImpl.class);

	@Resource
	private SignatureChecker signatureChecker;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.inc.forrest.aws.component.MessageSignatureComponent#check(java.lang
	 * .String, java.security.PublicKey)
	 */
	@Override
	public boolean check(String message, PublicKey publicKey) {

		return signatureChecker.verifyMessageSignature(message, publicKey);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.inc.forrest.aws.component.MessageSignatureComponent#getPublicKey(jp
	 * .inc.forrest.aws.service.dto.Message)
	 */
	@Override
	public PublicKey getPublicKey(Message msg) {

		URL url;
		InputStream inStream = null;
		X509Certificate cert = null;
		try {
			url = new URL(msg.getSigningCertURL());
			inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) cf.generateCertificate(inStream);
			inStream.close();
		} catch (CertificateException | IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return cert.getPublicKey();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.inc.forrest.aws.component.MessageSignatureComponent#health(java.lang
	 * .String)
	 */
	@Override
	public void health(String message) {
		logger.info("HELO " + message);
	}

}
