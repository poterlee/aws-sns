package jp.inc.forrest.aws.util;

import java.io.IOException;

import jp.inc.forrest.aws.service.dto.Message;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageReadUtil {

	public static Message readMessageFromJson(String json) {

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
