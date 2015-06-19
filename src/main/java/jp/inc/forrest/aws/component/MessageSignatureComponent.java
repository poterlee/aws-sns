package jp.inc.forrest.aws.component;

import java.security.PublicKey;

import jp.inc.forrest.aws.service.dto.Message;

/**
 * 電子署名検証コンポーネント
 *
 * @author kuhubgit
 *
 */
public interface MessageSignatureComponent {

	/**
	 * メッセージの署名妥当性を検証する。
	 *
	 * @param message
	 *            受信メッセージ文字列
	 * @param publicKey
	 *            公開鍵
	 * @return
	 */
	boolean check(String message, PublicKey publicKey);

	/**
	 * 公開鍵を取得する。
	 *
	 * @param msg
	 * @return 公開鍵
	 */
	PublicKey getPublicKey(Message msg);

	void hello(String message);

}
